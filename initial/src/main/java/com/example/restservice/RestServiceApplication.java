package com.example.restservice;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableAsync
public class RestServiceApplication {
	public static int MAX_THREADS = 4;
	public String[] RESULT_CONTENT = {};

	@Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

	public String callApi(String url) {
        ResponseEntity<String> response = this.restTemplate().getForEntity(url, String.class);
        return response.getBody();
    }

    @Bean
    public Executor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(this.MAX_THREADS);
        executor.setMaxPoolSize(this.MAX_THREADS);
        executor.setQueueCapacity(500);
        executor.initialize();
        return executor;
    }

	public static void main(String[] args) {
		SpringApplication.run(RestServiceApplication.class, args);
		/*ExecutorService pool = Executors.newFixedThreadPool(
			Math.min(
				RestServiceApplication.MAX_THREADS, Integer.parseInt(args[0])
			)
		);*/
	}
}
