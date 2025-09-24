package com.ut.crawler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ut.crawler.repository.SeedRepository;

import java.util.List;
import java.util.Random;

@Service
public class TopicSheduler {
    @Autowired
    private SeedFetchService seedFetchService;
    private final Random random = new Random();


    // Runs every ~60s with +/-30s jitter
   
    public void fetchTopicsWithJitter() {
        int jitter = random.nextInt(60000) - 30000; // -30s to +30s
        try {
            Thread.sleep(Math.max(0, jitter)); // Wait extra time before fetching
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        seedFetchService.extractTrends();
        
        System.out.println("Fetched topics: ");
    }
}
