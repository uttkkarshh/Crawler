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
import org.springframework.beans.factory.annotation.Autowired;

import com.ut.crawler.models.Comment;
import com.ut.crawler.models.Post;
import com.ut.crawler.models.Scroll;
import com.ut.crawler.models.Snip;
import com.ut.crawler.repository.PostRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BrowsingHelper {

	private static final Logger log = LoggerFactory.getLogger(BrowsingHelper.class);
	private WebDriver driver;
	 public BrowsingHelper(WebDriver driver) {
	        this.driver = driver;
	    }
	

	public void visitUrl(String url, int waitSeconds) {
		driver.get(url);

		try {
			new WebDriverWait(driver, Duration.ofSeconds(waitSeconds))
					.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState")
							.equals("complete"));
		} catch (TimeoutException e) {
			log.warn("⚠️ Page did not load fully within {} seconds: {}", waitSeconds, url);
		}
	}

	public String takeScreenshot(WebDriver driver) {
	    String outputPath = "screenshots/screenshot_" + System.currentTimeMillis() + ".png";
	    String compressedPath = "screenshots/compressed_" + System.currentTimeMillis() + ".jpg"; // compressed output

	    try {
	        // Ensure screenshots directory exists
	        new File("screenshots").mkdirs();

	        // Take screenshot as PNG first
	        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
	        File outputFile = new File(outputPath);
	        Files.copy(screenshot.toPath(), outputFile.toPath());

	        // Compress to JPEG with ~70% quality
	        File compressedFile = new File(compressedPath);
	        ImageCompressorUtils.compressToJpeg(outputFile, compressedFile, 0.7f);

	        log.info("📸 Screenshot captured: {} | Compressed version: {}", outputPath, compressedPath);

	        // Optionally delete the original PNG to save space
	        outputFile.delete();

	        return compressedPath;

	    } catch (IOException e) {
	        log.error("❌ Failed to take or compress screenshot: {}", e.getMessage(), e);
	        return null;
	    }
	}

	public Comment analyzePostContentAndComments(String mainContentSelector, String commentSelector,
			String platformName) {

		Comment commentPost = null;
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("window.scrollBy(0, 300);");
		try {
			List<WebElement> comments = new WebDriverWait(driver, Duration.ofSeconds(2))
					.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(commentSelector)));

			log.info("💬 Found {} comments", comments.size());
			
			int commentIndex = 1;
			for (WebElement commentElement : comments) {
				
				 log.info("➡️ Processing comment #{} | Location: {} | Size: {}x{}",
		                    commentIndex,
		                    commentElement.getLocation(),
		                    commentElement.getSize().getWidth(),
		                    commentElement.getSize().getHeight());

		            // Print out partial text and outer HTML for debug
		            String previewText = commentElement.getText();
		            if (previewText.length() > 50) {
		                previewText = previewText.substring(0, 50) + "...";
		            }
		            log.info("📝 Comment text preview: {}", previewText);

		            log.debug("🔍 Outer HTML snippet: {}", commentElement.getAttribute("outerHTML"));
				// Scroll into view
				forceScrollToElement(driver, commentElement, 100);

				// Wait until element is visible & stable
			    new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.visibilityOf(commentElement));
			


				String screenshotPath = this.takeScreenshot(driver);

				commentPost = new Comment();
				commentPost.setScreenshotPath(screenshotPath);

				log.info("✅ Captured comment #{}", commentIndex);

				if (commentIndex++ >= 5)
					break; // limit

				break;
			}

		} catch (TimeoutException e) {
			log.info("🗨️ No comments loaded for {}", platformName);
		} catch (Exception e) {
			log.error("❌ Error analyzing post and comments: {}", e.getMessage(), e);
		}

		return commentPost;
	}

	public Scroll scrollAndCaptureSnips(int count, int skip, String itemSelector, String titleSelector,
			String authorSelector, String platform, Map<String, List<String>> elementsToRemove) {

		Scroll scroll = new Scroll();
		JavascriptExecutor js = (JavascriptExecutor) driver;

		try {
			// 1. Clean distracting elements
			cleanPage(elementsToRemove, js);

			// 2. Fetch items
			List<WebElement> items = waitForElements(itemSelector, 3);

			// 3. Close modal if present
			closeModalIfPresent(js);

			// 4. Iterate posts
			for (int i = 0; i < count && i < items.size(); i++) {
				WebElement item = items.get(i);

				try {
					// Ensure visibility
					

					// Extract post info
					Post post = createPostFromElement(item, titleSelector, authorSelector, js);
					if (post == null)
						continue;

					Snip snip = new Snip();
					snip.addPost(post);
					snip.setScroll(scroll);
					String existingDesc = scroll.getDescription() != null ? scroll.getDescription() : "";
					String postInfo = "\n- " 
						    + post.getTitle().substring(0, Math.min(10, post.getTitle().length())) 
						    + " (by " 
						    + post.getAuthor().substring(0, Math.min(5, post.getAuthor().length())) 
						    + ")";

				    scroll.setDescription(existingDesc + postInfo);
				    log.info(scroll.getDescription());
					// Only screenshot every 3rd post
					if (i % skip == 0) {
						scrollIntoViewAndWait(item, js);
						String screenshotPath = this.takeScreenshot(driver);
						snip.setScreenshotPath(screenshotPath);
						log.info("📸 Captured screenshot for post {}: {} by {}", i, post.getTitle(), post.getAuthor());
					} else {
						log.info("📝 Captured text-only snip {}: {} by {}", i, post.getTitle(), post.getAuthor());
					}

					scroll.addSnip(snip);

				} catch (Exception e) {
					log.warn("⚠️ Skipped item at index {}: {}", i, e.getMessage());
				}
			}

		} catch (TimeoutException e) {
			log.warn("⚠️ No elements found for selector: {}", itemSelector);
		} catch (Exception e) {
			log.error("❌ Error while capturing snips: {}", e.getMessage(), e);
		}

		return scroll;
	}

	public List<String> getTextByCssSelector(String cssSelector, int limit) {
		List<String> results = new ArrayList<>();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

		try {
			List<WebElement> elements = wait
					.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(cssSelector)));

			int index = 0;
			for (WebElement element : elements) {
				String text = element.getText().trim();
				if (!text.isEmpty()) {
					results.add(text);
					log.info("📌 Element text: {}", text);
				}
				if (limit > 0 && ++index >= limit)
					break;
			}
		} catch (TimeoutException e) {
			log.warn("⏳ Timeout: No elements found for selector {}", cssSelector);
		} catch (Exception e) {
			log.error("❌ Error while fetching text for selector {}: {}", cssSelector, e.getMessage(), e);
		}

		return results;
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

	public String getCurrentUrl() {
		return driver.getCurrentUrl();
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

	public void executeJs(String js) {
		JavascriptExecutor jsExecuter = (JavascriptExecutor) driver;
		jsExecuter.executeScript(js);
	}

	public void login(WebDriver driver, String email, String password) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

		try {
			// this.takeScreenshot(driver);
			// Step 1: Enter email
			WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input")));
			emailInput.sendKeys(email);
			// this.takeScreenshot(driver);
			// Step 2: Click Next after email
			WebElement nextButton = wait.until(
					ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Next']/ancestor::button\r\n")));
			nextButton.click();
			// this.takeScreenshot(driver);

			// Step 3: Wait for password field
			WebElement passwordInput = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(" input[name='password']")));
			passwordInput.sendKeys(password);

			// Step 4: Click Next/Login button
			WebElement passwordNextBtn = wait.until(
					ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Login']/ancestor::button\r\n")));
			// this.takeScreenshot(driver);
			passwordNextBtn.click();
			this.takeScreenshot(driver);
		} catch (TimeoutException e) {
			System.out.println("Timed out while trying to log in: " + e.getMessage());
		} catch (NoSuchElementException e) {
			System.out.println("Element not found: " + e.getMessage());
		}
	}

	private void cleanPage(Map<String, List<String>> elementsToRemove, JavascriptExecutor js) {
		elementsToRemove.forEach((type, names) -> {
			for (String name : names) {
				if ("id".equalsIgnoreCase(type)) {
					removeElementById(name, js);
				} else if ("tag".equalsIgnoreCase(type)) {
					removeElementByTag(name, js);
				}
			}
		});
	}

	private List<WebElement> waitForElements(String selector, int timeoutSeconds) {
		return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
				.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(selector)));
	}

	private void closeModalIfPresent(JavascriptExecutor js) {
		js.executeScript("let modal = document.querySelector('div[role=\"dialog\"]'); if (modal) modal.remove();");
	}

	private boolean scrollIntoViewAndWait(WebElement element, JavascriptExecutor js) {
		try {
			js.executeScript("arguments[0].scrollIntoView({behavior:'instant', block:'center'});", element);
			new WebDriverWait(driver, Duration.ofSeconds(3)).until(ExpectedConditions.visibilityOf(element));
			return true;
		} catch (TimeoutException e) {
			return false;
		}
	}
	
	
	private boolean forceScrollToElement(WebDriver driver, WebElement element, int offset) {
	    try {
	        JavascriptExecutor js = (JavascriptExecutor) driver;

	        Number beforeScroll = (Number) js.executeScript("return window.scrollY;");
	        log.info("📍 Before scroll: {}", beforeScroll.longValue());

	        js.executeScript(
	            "window.scrollTo(0, arguments[0].getBoundingClientRect().top + window.scrollY - arguments[1]);",
	            element, offset
	        );

	        Thread.sleep(1000); // Let the page settle/render

	        Number afterScroll = (Number) js.executeScript("return window.scrollY;");
	        log.info("📍 After scroll: {}", afterScroll.longValue());

	        new WebDriverWait(driver, Duration.ofSeconds(9))
	                .until(ExpectedConditions.visibilityOf(element));

	        return true;
	    } catch (Exception e) {
	        log.error("❌ Error while scrolling: {}", e.getMessage(), e);
	        return false;
	    }
	}

}
