package br.com.spring.redis.redis_jedis.repository;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import br.com.spring.redis.redis_jedis.entity.Produto;

/**
 * Classe utilizada para simular um repositório de dados relacionais
 */
@Repository
public class ProdutoRepository {

    private static final int QUANTIDADE_PRODUTOS = 100;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProdutoRepository.class);

    private List<Produto> produtos = new ArrayList<>();

    public ProdutoRepository() {
        this.populaProdutos();
    }

    private void populaProdutos() {
        for(int i=0; i<QUANTIDADE_PRODUTOS; i++) {
            this.produtos.add(new Produto(i, String.format("Produto #%d", i), i * 1000));
        }
        LOGGER.info(String.format("Produtos populados com sucesso"));
    }

    public List<Produto> buscarTodos() {
        LOGGER.info("Realizando pesquisa de todos os produtos no repositório");
        LOGGER.info(String.format("Produtos encontrados: %d", this.produtos.size()));
        return this.produtos;
    }

}
