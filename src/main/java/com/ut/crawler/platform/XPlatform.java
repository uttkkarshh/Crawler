package com.ut.crawler.platform;

import com.ut.crawler.models.Scroll;
import com.ut.crawler.models.Topic;
import com.ut.crawler.utils.BrowsingHelper;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class XPlatform implements Platform {

    private static final Logger log = LoggerFactory.getLogger(XPlatform.class);
    private final BrowsingHelper helper = new BrowsingHelper();

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.X;
    }

    @Override
    public Scroll crawl(Topic topic) {
        System.out.println("📥 Crawling topic on X: " + topic.getName());
        return searchKeyword(topic.getName());
    }

    public Scroll searchKeyword(String keyword) {
        log.info("🔍 Searching X.com for keyword: {}", keyword);
       
        WebDriver driver = helper.createHeadlessDriver();
        Scroll scroll = null;

        try {
            String url = "https://twitter.com/search?q=" + keyword.replace(" ", "%20") + "&src=typed_query&f=live";
            helper.visitUrl(url, 5); // Give more wait time due to heavy JS loading
            helper.resizeWindow();
            helper.login(driver,"ramlal626026@gmail.com","hesoyam7");
            // Remove elements that block view or popups (e.g., login/signup modals)
            Map<String, List<String>> elementsToRemove = new HashMap<>();
            elementsToRemove.put("tag", Arrays.asList("header", "aside")); // Remove nav bar and sidebars
            elementsToRemove.put("class", Arrays.asList("r-1j3t67a")); // Possible pop-up/login overlay

            scroll = helper.scrollAndCaptureSnips(
                driver,
                20,                                // count
                3,                                 // skip
                "article[data-testid='tweet']",    // itemSelector
                "div[data-testid='tweetText']",    // titleSelector (tweet text)
                "div[dir='ltr'] span",             // authorSelector (name/handle; refine if needed)
                "X",                               // platform name
                elementsToRemove
            );

        } catch (Exception e) {
            log.error("❌ Error searching X.com for keyword '{}': {}", keyword, e.getMessage(), e);
        } finally {
            helper.quit();
        }

        return scroll;
    }

    public boolean isAvailable() {
        return true;
    }
}
