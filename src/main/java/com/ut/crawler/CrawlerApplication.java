package com.ut.crawler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;


import com.ut.crawler.service.CrawlServiceImpl;

import jakarta.annotation.PreDestroy;

@SpringBootApplication
@EnableScheduling
public class CrawlerApplication {

    
	public static void main(String[] args) {
		SpringApplication.run(CrawlerApplication.class, args);
	    
	}
	   @Component
	    public static class AppRunner {
           @Autowired
	        private final CrawlServiceImpl crawlerService;

	        
	        public AppRunner(CrawlServiceImpl crawlerService) {
	            this.crawlerService = crawlerService;
	        }

	        @EventListener(ApplicationReadyEvent.class)
	        public void run() {
	        	 System.setProperty("webdriver.chrome.driver", "C:\\Users\\utkar\\Downloads\\tools\\chromedriver.exe");
	            
	             crawlerService.crawlPlatform();
	        }

	        @PreDestroy
	        public void onShutdown() {
	            System.out.println("🛑 Shutting down crawler...");
	            crawlerService.shutdown();
	        }
	    }
}
