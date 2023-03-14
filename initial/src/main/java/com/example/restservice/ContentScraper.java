package com.example.restservice;

public record ContentScraper(
    long id,
    String[] extractedHeaders,
    int successfulCalls,
    int unsuccessfulCalls,
    int totalCalls
) { }