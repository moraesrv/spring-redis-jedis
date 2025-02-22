package br.com.spring.redis.redis_jedis.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.spring.redis.redis_jedis.entity.Produto;
import br.com.spring.redis.redis_jedis.repository.ProdutoRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Classe utilizada para manipular os dados no Redis
 */
@Service
public class ProdutoService {

    private static final String PREFIXO_CHAVE_PRODUTO = "produto";
    private static final Logger LOGGER = LoggerFactory.getLogger(ProdutoService.class);
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ProdutoRepository produtoRepository;

    /**
     * Método responsável por pesquisar múltiplos produtos do Redis
     * @param ids relação de ids do produtos a serem pesquisados
     * @return lista de produtos encontrados
     */
    public List<Produto> buscarMultiplosProdutosPelaChave(Set<Integer> ids) {
        LOGGER.info("Solicitada a pesquisa de múltiplas chaves");

        Set<String> chaves = ids.stream().map(id -> getChave(id)).collect(Collectors.toSet());
        List<String> produtosSerializados = redisService.consultarMultiplasChaves(chaves);

        List<Produto> produtos = new ArrayList<>();
        try {
            for (String produto : produtosSerializados) {
                produtos.add(objectMapper.readValue(produto, Produto.class));
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("Ocorreu um erro ao pesquisar as múltiplas chaves", e);
        }
        return produtos;
    }

    /**
     * Método responsável por excluir múltiplos produtos do Redis
     * @param ids relação de ids do produtos a serem removidos
     */
    public void removerMultiplosProdutosPelaChave(Set<Integer> ids) {
        LOGGER.info("Solicitada a remoção de múltiplas chaves");
        Set<String> chaves = ids.stream().map(id -> getChave(id)).collect(Collectors.toSet());
        redisService.removerMultiplasChaves(chaves);
    }

    /**
     * Método que retorna todos os produtos cadastrados no Redis
     * @return
     */
    public List<Produto> buscarTodos() {
        LOGGER.info("Solicitada a pesquisa de todos os produtos");
        return produtoRepository.buscarTodos();
    }

    /**
     * Métodos responsável por excluir todos os registros do cache
     */
    public void limparCache() {
        LOGGER.info(String.format("Solicitada limpeza do cache"));
        redisService.limparCache();
    }

    /**
     * Método responsável por pesquisar um produto pelo seu id no Redis
     * @param idProduto identificação do produto
     * @return dados do produto
     */
    public Produto consultar(int idProduto) {
        Produto produto = null;
        String chave = getChave(idProduto);
        try {
            LOGGER.info(String.format("Solicitada a pesquisa do produto com chave [%s]", chave));
            String produtoSerializado = redisService.consultar(chave);
            produto = objectMapper.readValue(produtoSerializado, Produto.class);
        } catch (JsonMappingException e) {
            LOGGER.error(String.format("Ocorreu um erro ao pesquisar o produto com chave [%s]", chave), e);
        } catch (JsonProcessingException e) {
            LOGGER.error(String.format("Ocorreu um erro ao pesquisar o produto com chave [%s]", chave), e);
        }
        return produto;
    }

    /**
     * Método responsável por inserir/atualizar os dados de um produto no Redis
     * @param produto dados do produto
     */
    public void salvar(Produto produto) {
        try {
            String chave = getChave(produto.getId());
            String valor = objectMapper.writeValueAsString(produto);
            LOGGER.info(String.format("Solicitada a persistência do produto com chave [%s]", chave));
            redisService.salvar(chave, valor);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método responsável por excluir um produto do Redis
     * @param idProduto identificação do produto
     */
    public void remover(int idProduto) {
        String chave = getChave(idProduto);
        LOGGER.info(String.format("Solicitada a remoção do produto com chave [%s]", chave));
        redisService.remover(chave);
    }

    /**
     * Método responsável por gerar a chave do produto
     * @param idProduto identificação do produto
     * @return chave do produto
     */
    private String getChave(int idProduto) {
        return String.format("%s::%d", PREFIXO_CHAVE_PRODUTO, idProduto);
    }

    /**
     * Método responsável por realizar o bulk de uma lista de produtos no Redis
     * @param produtos lista de produtos
     */
    public void popularCacheRedis(List<Produto> produtos) {
        LOGGER.info(String.format("Solicitado bulk de registros [Quantidade: %d]", produtos.size()));
        Map<String, String> map = new HashMap<String, String>();
        for (Produto produto : produtos) {
            try {
                map.put(
                    getChave(produto.getId()),
                    objectMapper.writeValueAsString(produto)
                );
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        redisService.executarBulk(map);
    }
}
