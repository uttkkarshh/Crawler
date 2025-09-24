package com.ut.crawler.utils;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ut.crawler.config.WebDriverProperties;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class WebDriverPool {

    private static final Logger logger = LoggerFactory.getLogger(WebDriverPool.class);

    private final BlockingQueue<WebDriver> pool;
    private final WebDriverFactory factory;
    private final WebDriverProperties properties;

    public WebDriverPool(WebDriverFactory factory, WebDriverProperties properties) {
        this.factory = factory;
        this.properties = properties;
        int poolSize = properties.getPoolSize(); // comes from application.yml/properties

        this.pool = new LinkedBlockingQueue<>(poolSize);

        // if path is provided, set it
        if (properties.getChromePath() != null && !properties.getChromePath().isBlank()) {
            System.setProperty("webdriver.chrome.driver", properties.getChromePath());
            logger.info("✅ ChromeDriver path set to: {}", properties.getChromePath());
        }

        // Initialize pool with fresh drivers
        logger.info("🚀 Initializing WebDriverPool with size: {}", poolSize);
        for (int i = 0; i < poolSize; i++) {
            WebDriver driver = factory.createDriver();
            pool.offer(driver);
            logger.info("🆕 Created WebDriver instance [{}] and added to pool.", i + 1);
        }
        logger.info("🎉 WebDriverPool initialization complete with {} drivers.", poolSize);
    }

    /** Borrow a WebDriver (blocks if none available) */
    public WebDriver getDriver() throws InterruptedException {
        WebDriver driver = pool.take();
        logger.info("📤 Borrowed WebDriver from pool. Remaining available: {}", pool.size());
        return driver;
    }

    /** Return driver to pool */
    public void releaseDriver(WebDriver driver) {
        if (driver != null) {
            pool.offer(driver);
            logger.info("📥 Returned WebDriver to pool. Available now: {}", pool.size());
        }
    }

    /** Graceful shutdown */
    @PreDestroy
    public void shutdown() {
        logger.info("🛑 Shutting down WebDriverPool. Closing all drivers...");
        for (WebDriver driver : pool) {
            try {
                driver.quit();
                logger.info("✅ Closed WebDriver.");
            } catch (Exception e) {
                logger.warn("⚠️ Failed to close WebDriver: {}", e.getMessage());
            }
        }
        logger.info("♻️ WebDriverPool shutdown complete.");
    }
}
