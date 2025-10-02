package com.ut.crawler.service;




import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.ut.crawler.models.Seed;
import com.ut.crawler.repository.SeedRepository;
import com.ut.crawler.utils.BrowsingHelper;
import com.ut.crawler.utils.WebDriverPool;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeedFetchService {
	 
	  private final WebDriverPool driverPool;
	  private  BrowsingHelper helper=null;
      Logger logger = LoggerFactory.getLogger(getClass());
      private WebDriver driver = null;
    public SeedFetchService(WebDriverPool driverPool) {
    	
	    driver = null;
		this.driverPool = driverPool;

		try {
			driver = driverPool.getDriver();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        helper = new BrowsingHelper(driver);
    	
    }

    @Autowired
    private SeedRepository seedRepository;
    
    public void fetchTrendingTopics() {
       

        // Categories to fetch
        List<Integer> categories = List.of(10, 2, 4, 6);
      //List<Integer> categories = List.of(10);
        for (Integer category : categories) {
            String url = "https://trends.google.com/trending?geo=IN&hours=24&category=" + category;
            helper.visitUrl(url, 1);
            extractTrends();
            
        }

    	driverPool.releaseDriver(driver);
		logger.info(" Driver Released from seed.");
    }
   
    public  void  extractTrends() {
    	
        List<String> secondColumnTexts = helper.getTextByCssSelector("table tr td:nth-child(2) div",5);
        logger.info("🔥 Extracted Trends: {}", secondColumnTexts);
        saveSeedsForPlatforms(secondColumnTexts);
        
    }
   
   
    public void saveSeedsForPlatforms(List<String> topics) {
      

        if (topics == null || topics.isEmpty()) {
            logger.warn("No topics provided. Seeds will not be saved.");
            return;
        }

        List<Seed> seeds = new ArrayList<>();

        try {
            for (String topic : topics) {
                if (topic == null || topic.trim().isEmpty()) {
                    logger.warn("Skipping empty topic.");
                    continue;
                }

                seeds.add(new Seed(topic.trim(), "YOUTUBE"));
                seeds.add(new Seed(topic.trim(), "REDDIT"));
            }

            if (!seeds.isEmpty()) {
                seedRepository.saveAll(seeds);
                logger.info("Successfully saved {} seeds for topics: {}", seeds.size(), topics);
            } else {
                logger.warn("No valid seeds to save after filtering topics.");
            }

        } catch (DataAccessException e) {
            logger.error("Database error while saving seeds: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while saving seeds: {}", e.getMessage());
        }
    }

}
