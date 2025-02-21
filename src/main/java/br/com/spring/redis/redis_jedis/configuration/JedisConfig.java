package br.com.spring.redis.redis_jedis.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Classe de configuração do Jedis
 */
@Configuration
public class JedisConfig {
    
    private static final String REDIS_SERVIDOR = "localhost";
    private static final int REDIS_PORTA = 6379;
    private static final int REDIS_TIMEOUT = 2000;
    private static final String REDIS_SENHA = "root";

    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);        // Número máximo de conexões
        poolConfig.setMaxIdle(5);          // Número máximo de conexões ociosas
        poolConfig.setMinIdle(1);          // Número mínimo de conexões ociosas
        poolConfig.setTestOnBorrow(true);  // Testa a conexão antes de usá-la
        return new JedisPool(poolConfig,REDIS_SERVIDOR, REDIS_PORTA, REDIS_TIMEOUT, REDIS_SENHA);
    }

    @Bean
    public Jedis jedis(JedisPool jedisPool) {
        return jedisPool.getResource();
    }
}
