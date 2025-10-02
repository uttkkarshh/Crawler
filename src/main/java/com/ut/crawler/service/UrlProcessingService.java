package com.ut.crawler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ut.crawler.crawler.Crawler;
import com.ut.crawler.models.Comment;
import com.ut.crawler.models.CrawlUrl;
import com.ut.crawler.models.Post;
import com.ut.crawler.models.PriorityLevel;
import com.ut.crawler.models.Scroll;
import com.ut.crawler.models.Snip;
import com.ut.crawler.models.Topic;
import com.ut.crawler.models.TypeOfUrl;
import com.ut.crawler.platform.PlatformContext;
import com.ut.crawler.queue.FrontQueueManager;
import com.ut.crawler.repository.ScrollRepository;
import com.ut.crawler.repository.TopicRepository;
import com.ut.crawler.utils.BrowsingHelper;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
@Service
public class UrlProcessingService {
    @Autowired
    private PostService postService;
    @Autowired
    private FrontQueueManager frontqueue;
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private ScrollRepository scrollRepository;
    @Autowired
    private MeterRegistry meterRegistry;
    @Autowired UrlFilterService urlFilterService;
    private static final Logger log = LoggerFactory.getLogger(Crawler.class);

    public void processSeed(PlatformContext platform, Topic topic, BrowsingHelper helper) throws Exception {
        Timer.Sample sample = Timer.start(meterRegistry); // start timing

        try {
            String searchUrl = platform.buildSearchUrl(topic.getName());
            helper.visitUrl(searchUrl, 2);
            helper.resizeWindow();

            Scroll scroll = helper.scrollAndCaptureSnips(
                10, 3,
                platform.getScrollSelector(),
                platform.getTitleSelector(),
                platform.getAuthorSelector(),
                platform.getName(),
                platform.getElementsToRemove()
            );

            for (Snip snip : scroll.getSnips()) {
                for (Post post : snip.getPosts()) {
                	  if (post.getUrl() != null && post.getUrl().contains("youtube.com/shorts")) {
                          continue;
                      }
                    frontqueue.enqueue(new CrawlUrl(
                        post.getUrl(),
                        PriorityLevel.HIGH,
                        platform.getName().toUpperCase(),
                        0,
                        TypeOfUrl.POST,
                        topic.getId()
                    ));
                }
            }
            scroll.setTopic(topic);
           
            topic.getScrolls().add(scroll);
            log.info("Scroll Desctiption {}",scroll.getDescription());
            topicRepository.save(topic);
            scrollRepository.save(scroll);

        } finally {
            sample.stop(
                Timer.builder("crawler.process.seed.time")
                     .tag("platform", platform.getName())
                     .tag("topic", topic.getName())
                     .description("Time spent processing a seed topic")
                     .register(meterRegistry)
            );
        }
    }

    public void processPost(PlatformContext platform, String postUrl, BrowsingHelper helper) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            helper.visitUrl(postUrl, 2);
            helper.resizeWindow();

            try {
             //   helper.executeJs("document.querySelector('video').pause();");
            } catch (Exception e) {
                // no video element
            }

            Comment comment = helper.analyzePostContentAndComments(
                platform.getPostContentSelector(),
                platform.getCommentSelector(),
                platform.getName()
            );

            if (comment != null) {
                postService.addCommentToPost(postUrl, comment);
            } else {
                log.info("🗨️ No comments found.");
            }
        } catch (Exception e) {
            log.error("❌ Error processing post: {}", postUrl, e);
        } finally {
            sample.stop(
                Timer.builder("crawler.process.post.time")
                     .tag("platform", platform.getName())
                     .description("Time spent processing a post")
                     .register(meterRegistry)
            );
        }
    }
}


