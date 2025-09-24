package com.ut.crawler.crawler;

import com.ut.crawler.platform.PlatformContext;
import com.ut.crawler.platform.PlatformRegistry;
import com.ut.crawler.platform.PlatformType;
import com.ut.crawler.queue.BackQueueManager;
import com.ut.crawler.queue.BackQueueRouter;
import com.ut.crawler.queue.FrontQueueManager;
import com.ut.crawler.repository.TopicRepository;
import com.ut.crawler.service.PostService;
import com.ut.crawler.service.SeedFetchService;
import com.ut.crawler.service.UrlProcessingService;
import com.ut.crawler.utils.WebDriverFactory;
import com.ut.crawler.utils.WebDriverPool;

import jakarta.annotation.PreDestroy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;

import org.springframework.stereotype.Service;



@Service
public class CrawlManager {

    private static final Logger log = LoggerFactory.getLogger(CrawlManager.class);
    private final List<Crawler> activeCrawlers = new CopyOnWriteArrayList<>();
    private final WebDriverPool driverPool;
    private final BackQueueManager backqueue;
    private final BackQueueRouter backQueueRouter;
    private final UrlProcessingService urlProcessingService;
    private final SeedFetchService seedFetch;
    private final PlatformRegistry platformRegistry;
    private final TaskExecutor crawlerExecutor;
    public CrawlManager(
            BackQueueManager backqueue,
            FrontQueueManager frontqueue,
            BackQueueRouter backQueueRouter,
            UrlProcessingService urlProcessingService,
            SeedFetchService seedFetch,
            PlatformRegistry platformRegistry,TaskExecutor crawlerExecutor
            ,WebDriverPool driverPool
    ) {
        this.driverPool = driverPool;
		this.backqueue = backqueue;
        this.backQueueRouter = backQueueRouter;
        this.urlProcessingService = urlProcessingService;
        this.seedFetch = seedFetch;
		this.platformRegistry = platformRegistry;
		this.crawlerExecutor = crawlerExecutor;
        
    }

   

    /**
     * Starts crawling for all registered platforms asynchronously
     */
    public void startCrawlingForTopic(TopicRepository topicRepository, PostService postService) {
        log.info("🚀 Starting crawling for topics...");
        seedFetch.fetchTrendingTopics();

        // Launch back queue router in async mode
        startBackQueueRouter();

        // Launch crawlers asynchronously for each platform
        platformRegistry.getAllPlatforms().values().forEach(context -> {
            if (context.isEnabled()) {
                startCrawler(context);
            }
        });
    }

   
    public void startCrawler(PlatformContext context) {
    	try {
        Crawler crawler = new Crawler(context, backqueue, urlProcessingService,driverPool,platformRegistry);
        activeCrawlers.add(crawler);
        crawlerExecutor.execute(crawler);
    
          
        } catch (Exception e) {
            log.error("❌ Error in crawler for platform {}", context.getName(), e);
        }
    }
  
    
    public void startBackQueueRouter() {
        log.info("📦 Starting BackQueueRouter...");
        try {
            backQueueRouter.run();
        } catch (Exception e) {
            log.error("❌ Error in BackQueueRouter", e);
        }
    }

    

    /**
     * Called during application shutdown
     */
    @PreDestroy
    public void shutdown() {
        log.info("🛑 Shutting down CrawlManager...");

        if (backQueueRouter != null) {
            backQueueRouter.stop();
        }
        activeCrawlers.forEach(Crawler::stop);
        // no need for executor.shutdownNow() here — Spring manages the pool

        log.info("✅ CrawlManager shutdown complete.");
    }
}
