package br.com.spring.redis.redis_jedis;

import java.util.List;
import java.util.Set;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import br.com.spring.redis.redis_jedis.entity.Produto;
import br.com.spring.redis.redis_jedis.service.ProdutoService;

@SpringBootApplication
public class RedisJedisApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisJedisApplication.class, args);
	}

	@Bean
	ApplicationRunner runner(ProdutoService produtoService) {
		return args -> {

			// Obtém todos os produtos que estão em um repositório
			List<Produto> produtos = produtoService.buscarTodos();
			
			// Limpar cache Redis
			produtoService.limparCache();
			
			// Popular no Redis todos os produtos encontrados
			produtoService.popularCacheRedis(produtos);

			// Inserir o produto
			produtoService.salvar(new Produto(100, "Produto 100", 100000));

			// Atualiza o produto
			produtoService.salvar(new Produto(0, "Produto 000", 100));

			// Remover um registro
			produtoService.remover(50);

			// Consultar um produto
			Produto produto = produtoService.consultar(0);
			if (produto != null) System.out.println(String.format("[Produto #%d, Nome: %s, Preco: %.2f]", produto.getId(), produto.getNome(), produto.getPreco()));

			// Pesquisa múltiplos produtos
			produtos = produtoService.buscarMultiplosProdutosPelaChave(Set.of(1, 2, 3, 4, 5));
			for (Produto p : produtos) {
				System.out.println(String.format("[Produto #%d, Nome: %s, Preco: %.2f]", p.getId(), p.getNome(), p.getPreco()));
			}

			// Remover múltiplos produtos
			produtoService.removerMultiplosProdutosPelaChave(Set.of(1, 2, 3, 4, 5));
		};
	}

}
