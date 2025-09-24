package com.ut.crawler.service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ut.crawler.crawler.CrawlManager;

import com.ut.crawler.platform.Platform;

import com.ut.crawler.platform.PlatformType;

import com.ut.crawler.repository.TopicRepository;

@Service
public class CrawlServiceImpl implements CrawlService {
	 
	 @Autowired
	 private TopicRepository topicRepository;
	 @Autowired
	 PostService postService;

	    @Autowired
	    private CrawlManager crawlManager;
	 Map<PlatformType,Platform> Platforms=new HashMap<PlatformType, Platform>(); 
	
	 
	 public void setPlatforms(List<Platform> platforms) {
		 for(Platform p:platforms) {
			 Platforms.put(p.getPlatformType(), p);
		 }
	 }
	 
	 
	 public void crawlPlatform() {
		
		    crawlManager.startCrawlingForTopic(topicRepository,postService);
		    
	      

			
	 }
	 
	 
	 public CrawlServiceImpl() {
	       
	    } 
	 
	
	   
	   


	public void shutdown() {
		// TODO Auto-generated method stub
		 crawlManager.shutdown();
	}
}
