package com.ut.crawler.controller;




import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ut.crawler.crawler.CrawlManager;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final CrawlManager crawlManager;

    @Autowired
    public TaskController(CrawlManager crawlManager) {
        this.crawlManager = crawlManager;
    }

    /**
     * End (stop) all crawling tasks
     */
    @PostMapping("/endAll")
    public String endAllTasks() {
        try {
            crawlManager.shutdown();
            return "✅ All crawling tasks have been stopped successfully.";
        } catch (Exception e) {
            return "❌ Failed to stop tasks: " + e.getMessage();
        }
    }
}
