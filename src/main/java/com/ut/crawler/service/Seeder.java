package com.ut.crawler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ut.crawler.models.CrawlUrl;
import com.ut.crawler.models.PriorityLevel;
import com.ut.crawler.models.Seed;
import com.ut.crawler.models.TypeOfUrl;
import com.ut.crawler.queue.FrontQueueManager;
import com.ut.crawler.repository.SeedRepository;

import java.util.List;

@Component
public class Seeder {
	 @Autowired
    private  FrontQueueManager frontQueue;
    @Autowired
    private SeedRepository seedRepository;
    private int noTopicCount=0;
   
    // Runs every 1 minute (60000 ms)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void seedFromDatabase() {
        List<Seed> seeds = seedRepository.findAll();

        if (seeds.isEmpty()) {
            System.out.println("📭 No seeds found in database.");
            noTopicCount++;
            if(noTopicCount==0) {
            	
            }
            return;
        }
        noTopicCount=0;
        seeds.forEach(seed -> {
            CrawlUrl url = new CrawlUrl(
                seed.getTopic(),
                PriorityLevel.HIGH,
                seed.getPlatform().toUpperCase(),
                0,
                TypeOfUrl.SEED
            ,-1L);
            frontQueue.enqueue(url);
            System.out.println("🌱 Seed added to front queue: " + url.getUrl());
        });

        // Remove processed seeds from DB
        seedRepository.deleteAll(seeds);
        System.out.println("🧹 Removed processed seeds from database.");
    }
}

