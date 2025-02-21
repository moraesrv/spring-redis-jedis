package br.com.spring.redis.redis_jedis.service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.spring.redis.redis_jedis.repository.ProdutoRepository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

@Service
public class RedisService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProdutoRepository.class);

    @Autowired
    private Jedis jedis;

    /**
     * Método responsável por consultar múltiplos registros no Redis
     * @param chaves chaves dos produtos
     * @return relação de produtos serializados
     */
    public List<String> consultarMultiplasChaves(Set<String> chaves) {
        LOGGER.info("Pesquisando múltiplas chaves no Redis!");
        return jedis.mget(chaves.toArray(new String[0]));
    }

    /**
     * Método responsável por remover múltiplos registros no Redis
     * @param chaves relação de chaves dos produtos
     */
    public void removerMultiplasChaves(Set<String> chaves) {
        LOGGER.info("Removendo múltiplas chaves no Redis!");
        jedis.del(chaves.toArray(new String[0]));
    }

    /**
     * Método responsável por pesquisar um registro no Redis pela sua chave
     * @param chave chave do registro
     * @return registro serializado
     */
    public String consultar(String chave) {
        LOGGER.info(String.format("Pesquisando chave [%s] no Redis!", chave));
        return jedis.get(chave);
    }

    /**
     * Método responsável por incluir/atualizar um registro no Redis
     * @param chave chave do registro
     * @param valor valor do registro
     */
    public void salvar(String chave, String valor) {
        LOGGER.info(String.format("Registro chave [%s] salvo no Redis com sucesso!", chave));
        jedis.set(chave, valor);
    }

    /**
     * Método responsável por remover um registro do Redis
     * @param chave chave do produto a ser removido
     * @return retorna se a remoção foi realizada com sucesso
     */
    public boolean remover(String chave) {
        long registroRemovido = jedis.del(chave);
        if (registroRemovido > 0) {
            LOGGER.info(String.format("Registro chave [%s] removido do Redis com sucesso!", chave));
            return true;
        }
        LOGGER.warn(String.format("Registro chave [%s] não encontrado no Redis!", chave));
        return false;
    }

    /**
     * Método responsável por limapar o cache
     */
    public void limparCache() {
        jedis.flushDB();
        LOGGER.info("Limpeza do cache no Redis realizada com sucesso!");
    }

    /**
     * Método responsável por realizar um bulk de registros
     * @param registros map de registros com chave e valor
     */
    public void executarBulk(Map<String, String> registros) {
        Pipeline pipeline = jedis.pipelined();
        try {
            LOGGER.info("Carregando os produtos na pipeline");
            Iterator<Entry<String, String>> it = registros.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<String, String> registro = (Map.Entry<String, String>) it.next();
                pipeline.set(registro.getKey(), registro.getValue());
            }
            LOGGER.info("Realizando bulk de objetos no Redis");
            pipeline.sync();
            LOGGER.info("Produtos cacheados no Redis com sucesso");
        } catch (Exception e) {
            LOGGER.error("Ocorreu um erro ao realizar o bulk no Redis", e);
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}

