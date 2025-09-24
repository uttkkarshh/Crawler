package com.ut.crawler.utils;


import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;
@Component
public class WebDriverFactory {

    public WebDriver createDriver() {
    	ChromeOptions options = new ChromeOptions();
		//options.addArguments("--headless=new");
//		options.addArguments("--no-sandbox");
//		options.addArguments("--disable-gpu");
	    options.addArguments("--disable-headless");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-gpu");
		options.addArguments("--disable-blink-features=AutomationControlled");
		options.addArguments(
				"user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/115 Safari/537.36");
		// options.addArguments("--blink-settings=imagesEnabled=false");
		options.addArguments("--disable-extensions");
		options.addArguments("--disable-popup-blocking");
		options.addArguments("--disable-notifications");

		Map<String, Object> prefs = new HashMap<>();
		prefs.put("media.autoplay.default", 1);
		prefs.put("media.block-autoplay-until-in-foreground", true);
		options.setExperimentalOption("prefs", prefs);

		return new ChromeDriver(options);
		
    }
}
