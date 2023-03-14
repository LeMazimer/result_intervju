package com.example.restservice;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.config.Task;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
public class ContentScraperController {
	private final AtomicLong counter = new AtomicLong();

	@GetMapping("/contentscraper")
	public ContentScraper result(
            @RequestParam(value = "concurrentCalls", defaultValue = "1") String concurrentCalls
        ) {
        String[] extractedHeaders = {};
        int totalCalls = 4;
        int successfulCalls = 0;
        int unsuccessfulCalls = 0;
        int maxCalls = 4;

        List<String> urls = Arrays.asList(
            "https://www.result.si/projekti/",
            "https://www.result.si/o-nas/",
            "https://www.result.si/kariera/",
            "https://www.result.si/blog/"
        );
        
        // transform string param into int
        int howManyCallsToMake = Integer.parseInt(concurrentCalls);

        // check input
        if ((1 <= howManyCallsToMake && howManyCallsToMake <= maxCalls) == false) {
            throw new Exception("bad params");
        }

        // totalCalls = A * howManyCallsToMake + totalCalls % howManyCallsToMake
        // A = quotient, totalCalls % howManyCallsToMake = remainder
        
        // prep pool
        ExecutorService pool = Executors.newFixedThreadPool(
            Math.min(howManyCallsToMake, maxCalls)
        );
        
        // in quotient part we do concurrent calls in pools of size howManyCallsToMake
        for (int i = 0; i < totalCalls / howManyCallsToMake; i++) {
            // make howManyCallsToMake concurrent calls
            for (int j = 0; j < howManyCallsToMake; j++){
                pool.execute(
                    new Task(howManyCallsToMake.toString())
                );
            }
            // wait for result
            pool.wait();
        }
        
        // in remainder part we do totalCalls % howManyCallsToMake concurrent calls
        for (int i = 0; i < totalCalls / howManyCallsToMake; i++) {
            // make concurrent calls
            pool.execute(
                new Task(howManyCallsToMake.toString())
            );
        }
        // wait for result
        pool.wait();

		return new ContentScraper(
            counter.incrementAndGet(),
            extractedHeaders,
            successfulCalls,
            unsuccessfulCalls,
            totalCalls
            );
	}
}