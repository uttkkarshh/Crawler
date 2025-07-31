package com.ut.crawler.queue;




import java.util.concurrent.ConcurrentLinkedQueue;

import com.ut.crawler.models.CrawlUrl;

public class FrontQueueManager {
    private final ConcurrentLinkedQueue<CrawlUrl> high = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<CrawlUrl> medium = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<CrawlUrl> low = new ConcurrentLinkedQueue<>();

    public void enqueue(CrawlUrl url) {
        switch (url.getPriority()) {
            case HIGH -> high.add(url);
            case MEDIUM -> medium.add(url);
            case LOW -> low.add(url);
        }
    }

    public CrawlUrl dequeueHighestPriority() {
        if (!high.isEmpty()) return high.poll();
        if (!medium.isEmpty()) return medium.poll();
        return low.poll();
    }

    public boolean isEmpty() {
        return high.isEmpty() && medium.isEmpty() && low.isEmpty();
    }
}
