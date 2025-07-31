package com.ut.crawler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.ut.crawler.models.Topic;
import com.ut.crawler.platform.PlatformType;
import com.ut.crawler.service.CrawlServiceImpl;

@SpringBootApplication
public class CrawlerApplication {

    private static final String LINKEDIN_URL = "https://www.linkedin.com";
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
	            
	             crawlerService.crawlPlatform(PlatformType.YOUTUBE,new Topic("MRBEAST"));
	        }
	    }
}
