package com.ut.crawler.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ut.crawler.models.Post;
import com.ut.crawler.models.Scroll;
import com.ut.crawler.models.Snip;
import com.ut.crawler.platform.YouTubePlatform;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BrowsingHelper {

	private static final Logger log = LoggerFactory.getLogger(BrowsingHelper.class);
    private WebDriver driver;
    
	public WebDriver createHeadlessDriver() {

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless=new");
//		options.addArguments("--no-sandbox");
//		options.addArguments("--disable-gpu");
//		options.addArguments("--disable-headless");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-gpu");
		options.addArguments("--disable-blink-features=AutomationControlled");
		options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/115 Safari/537.36");

		driver=new ChromeDriver(options);
		return driver;
	}

	public void visitUrl( String url, int waitSeconds) {
		driver.get(url);
		//String a = driver.getPageSource();
		//System.out.print(a);
		try {
			Thread.sleep(waitSeconds * 1000L); // or use WebDriverWait
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public String takeScreenshot(WebDriver driver) {
		String outputPath = "screenshots/screenshot_" + System.currentTimeMillis() + ".png";

		try {
			// Ensure screenshots directory exists
			new File("screenshots").mkdirs();

			File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			File outputFile = new File(outputPath);
			Files.copy(screenshot.toPath(), outputFile.toPath());

			System.out.println("📸 Screenshot saved to: " + outputPath);
			return outputPath;
		} catch (IOException e) {
			System.err.println("❌ Failed to take screenshot: " + e.getMessage());
			return null;
		}
	}

	

	public Scroll analyzePostContentAndComments(
		    String mainContentSelector,
		    String commentSelector,
		    String platformName
		) {
		    Scroll scroll = new Scroll();
		    JavascriptExecutor js = (JavascriptExecutor) driver;

		    try {
		        List<WebElement> comments = driver.findElements(By.cssSelector(commentSelector));
		        int commentIndex = 1;
		        int l=comments.size();
		        log.info("NO Comment #{}",l);
		        for (WebElement commentElement : comments) {
		            String commentText = js.executeScript("return arguments[0].innerText;", commentElement).toString();

		            // ✅ Scroll to this comment (not the whole list)
		            js.executeScript(
		                "window.scrollTo(0, arguments[0].getBoundingClientRect().top + window.scrollY - 10);",
		                commentElement);

		            Thread.sleep(2000); // Let the page settle/render after scroll

		            Post commentPost = new Post("Comment", driver.getCurrentUrl(), "Comment " + commentIndex);
		            String screenshotPath = this.takeScreenshot(driver);
                    
		            Snip commentSnip = new Snip();
		            commentSnip.setScreenshotPath(screenshotPath);
		            commentSnip.addPost(commentPost);
                    commentSnip.setScroll(scroll);
		            scroll.addSnip(commentSnip);
		            log.info("💬 Captured comment #{}", commentIndex);

		            commentIndex++;
		            if (commentIndex > 5) break; // optional limit
		        }

		    } catch (Exception e) {
		        log.error("❌ Error analyzing post and comments: {}", e.getMessage(), e);
		    }

		    return scroll;
		}


	public Scroll scrollAndCaptureSnips(WebDriver driver, int count, int skip, String itemSelector,
			String titleSelector, String authorSelector, String platform, Map<String, List<String>> elementsToRemove) {
		Scroll scroll = new Scroll();
		JavascriptExecutor js = (JavascriptExecutor) driver;
		
		for (Map.Entry<String, List<String>> entry : elementsToRemove.entrySet()) {
	        String type = entry.getKey();
	        for (String name : entry.getValue()) {
	            if ("id".equalsIgnoreCase(type)) {
	                removeElementById(name, js);
	            } else if ("tag".equalsIgnoreCase(type)) {
	                removeElementByTag(name, js);
	            }
	        }
	    }
		try {
			List<WebElement> items = driver.findElements(By.cssSelector(itemSelector));
			((JavascriptExecutor) driver).executeScript(
				    "let modal = document.querySelector('div[role=\"dialog\"]');" +
				    "if (modal) { modal.remove(); }"
				);

			int index = 0;
			for (int i = 0; i < count; i++) {
				if (index >= items.size()) {
					log.warn("No more elements to scroll at index " + index);
					break;
				}

				WebElement item = items.get(index);

				((JavascriptExecutor) driver).executeScript(
						"window.scrollTo(0, arguments[0].getBoundingClientRect().top + window.scrollY - 20);", item);

				Thread.sleep(2000); // Wait for scroll and rendering

				try {

					Post post = createPostFromElement(item, titleSelector, authorSelector, js);
					if (post == null)
						continue;

					// Create Snip and add Post
					String currentScreenshotPath = this.takeScreenshot(driver);
					Snip snip = new Snip();
					snip.setScreenshotPath(currentScreenshotPath); // You should define this value accordingly
					snip.addPost(post);
                    snip.setScroll(scroll);
					scroll.addSnip(snip);

					log.info("✅ Captured snip: {} by {}", post.getTitle(), post.getAuthor());

				} catch (Exception e) {
					log.warn("⚠️ Skipped item due to missing selectors at index {}", index);
				}

				index += skip;
			}
		} catch (Exception e) {
			log.error("❌ Error while capturing snips: " + e.getMessage(), e);
		}

		return scroll;
	}

	private Post createPostFromElement(WebElement item, String titleSelector, String authorSelector,
			JavascriptExecutor js) {
		try {
		    String title = "";
		    String videoUrl = "";

		    if (!titleSelector.isEmpty()) {
		        WebElement titleEl = item.findElement(By.cssSelector(titleSelector));
		        title = titleEl.getText();
		        videoUrl = titleEl.getAttribute("href");
		    } else {
		        // Fallback if no title selector provided
		        title = item.getText();
		        videoUrl = ""; // or extract in another way if needed
		    }

		    String author = "";
		    if (!authorSelector.isEmpty()) {
		        WebElement authorEl = item.findElement(By.cssSelector(authorSelector));
		        author = (String) js.executeScript("return arguments[0].textContent;", authorEl);
		    }

		    return new Post(title, videoUrl, author);
		} catch (Exception e) {
		    System.err.println("❌ Error creating post: " + e.getMessage());
		    return null;
		}

	}

	
	
	
	public void waitForElement(WebDriver driver, String cssSelector, int timeoutSeconds) {
		new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
				.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssSelector)));
	}

	public void resizeWindow() {
		try {
			int width = 1920;
			int height = 1080;
			driver.manage().window().setSize(new Dimension(width, height));
			System.out.println("✅ Browser window resized to " + width + "x" + height);
		} catch (Exception e) {
			System.err.println("❌ Failed to resize browser window: " + e.getMessage());
		}
	}

	private void removeElementByTag(String tagName, JavascriptExecutor js) {
		js.executeScript("let el = document.querySelector(arguments[0]); if (el) el.style.display = 'none';", tagName);
	}

	public void removeElementById(String id, JavascriptExecutor js) {
		js.executeScript("var el = document.getElementById(arguments[0]); if (el) el.style.display = 'none';", id);
	}

	public void quit() {
		if (driver != null) {
			driver.quit();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	public  void login(WebDriver driver, String email, String password) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
        	// this.takeScreenshot(driver);
            // Step 1: Enter email
            WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input")
            ));
            emailInput.sendKeys(email);
      //      this.takeScreenshot(driver);
            // Step 2: Click Next after email
            WebElement nextButton = wait.until(ExpectedConditions.elementToBeClickable(
            	    By.xpath("//span[text()='Next']/ancestor::button\r\n")
            	));
            	nextButton.click();
          //  	 this.takeScreenshot(driver);

            // Step 3: Wait for password field
            WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(" input[name='password']")
            ));
            passwordInput.sendKeys(password);

            // Step 4: Click Next/Login button
            WebElement passwordNextBtn = wait.until(ExpectedConditions.elementToBeClickable(
            		 By.xpath("//span[text()='Login']/ancestor::button\r\n")
            ));
        //	 this.takeScreenshot(driver);
            passwordNextBtn.click();
        	 this.takeScreenshot(driver);
        } catch (TimeoutException e) {
            System.out.println("Timed out while trying to log in: " + e.getMessage());
        } catch (NoSuchElementException e) {
            System.out.println("Element not found: " + e.getMessage());
        }
    }
}
