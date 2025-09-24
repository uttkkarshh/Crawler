package com.ut.crawler.utils;

import com.ut.crawler.models.Comment;
import com.ut.crawler.models.Scroll;

import java.util.List;
import java.util.Map;


public interface WebHelper {

    void visitUrl(String url, int waitSeconds);

    String takeScreenshot(Object driver); // Object allows Selenium or other driver types

    Comment analyzePostContentAndComments(String mainContentSelector, String commentSelector,
                                          String platformName);

    Scroll scrollAndCaptureSnips(int count, int skip, String itemSelector, String titleSelector,
                                 String authorSelector, String platform, Map<String, List<String>> elementsToRemove);

    List<String> getTextByCssSelector(String cssSelector, int limit);

    String getCurrentUrl();

    void waitForElement(Object driver, String cssSelector, int timeoutSeconds);

    void resizeWindow();

    void quit();

    void executeJs(String js);

    void login(Object driver, String email, String password);
}
