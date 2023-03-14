package com.example.restservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@RestController
public class RestServiceApplication {
    private static final AtomicLong counter = new AtomicLong();
    private final List<String> urls = Arrays.asList(
        "https://www.result.si/projekti/",
		"https://www.result.si/o-nas/",
		"https://www.result.si/kariera/",
		//"https://www.result.si/blog/"
		"krneki"
    );

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    private String callApi(String url) {
        return restTemplate().getForObject(url, String.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(RestServiceApplication.class, args);
    }

    @GetMapping("/contentscraper/{maxCalls}")
    public ContentScraper callApiConcurrently(@PathVariable int maxCalls) {
        ExecutorService executor = Executors.newFixedThreadPool(maxCalls);

        List<CompletableFuture<String>> futures = urls.stream()
            .map(url -> CompletableFuture.supplyAsync(() -> callApi(url), executor))
            .collect(Collectors.toList());

        //List<String> results = futures.stream()
        //    .map(CompletableFuture::join)
        //    .collect(Collectors.toList());
		
		List<String> results = futures.stream()
			.map(future -> {
				try {return future.join();} catch (Exception e) { return "";}
			})
			.collect(Collectors.toList());

        executor.shutdown();

		int successfulCalls = 0;
		int unsuccessfulCalls = 0;
		
		// prep default value
		String[] titles = new String[results.size()];
		Arrays.fill(titles, "NO_TITLE_FOUND");
		
		// html tag pattern
        Pattern pattern = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

		int i = 0;

        for (String result : results) {
            Matcher matcher = pattern.matcher(result);
            if (matcher.find()) {
                String title = matcher.group(1);
                titles[i] = title;
				successfulCalls ++;
            }
			else {
				unsuccessfulCalls ++;
			}
			i ++;
        }
		
        return new ContentScraper(
			this.counter.incrementAndGet(),
			titles,
			successfulCalls,
			unsuccessfulCalls,
			successfulCalls + unsuccessfulCalls
		);
    }

}
