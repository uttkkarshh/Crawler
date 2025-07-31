package com.ut.crawler.service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ut.crawler.crawler.CrawlManager;
import com.ut.crawler.crawler.Crawler;
import com.ut.crawler.models.Scroll;
import com.ut.crawler.models.Topic;
import com.ut.crawler.platform.Platform;
import com.ut.crawler.platform.PlatformContext;
import com.ut.crawler.platform.PlatformType;
import com.ut.crawler.platform.RedditPlatform;
import com.ut.crawler.platform.XPlatform;
import com.ut.crawler.platform.YouTubePlatform;
import com.ut.crawler.repository.TopicRepository;
import com.ut.crawler.utils.BrowsingHelper;
@Service
public class CrawlServiceImpl implements CrawlService {
	 private final BrowsingHelper browser;
	 @Autowired
	 private TopicRepository topicRepository;
	 

	    @Autowired
	    private CrawlManager crawlManager;
	 Map<PlatformType,Platform> Platforms=new HashMap<PlatformType, Platform>(); 
	 Platform p=new XPlatform();
	 
	 public void setPlatforms(List<Platform> platforms) {
		 for(Platform p:platforms) {
			 Platforms.put(p.getPlatformType(), p);
		 }
	 }
	 
	 
	 public void crawlPlatform(PlatformType type,Topic topic) {
		
		    crawlManager.startCrawlingForTopic(topic);
		    
	        topicRepository.save(topic);

			
	 }
	 
	 
	 public CrawlServiceImpl() {
	        this.browser = new BrowsingHelper();
	    } 
	 
	   public void capture(String url, String outputPath) {
	        WebDriver driver = browser.createHeadlessDriver();
	        try {
	            browser.visitUrl( url, 3);
	            
	           
	           // browser.takeScreenshot(driver, outputPath);
	        } finally {
	            browser.quit();
	        }
	    }
	   
	   public void searchKey(String head) {
		   Topic topic=new Topic();
		   topic.setName(head);
		   p.crawl(topic);
	   }
}
