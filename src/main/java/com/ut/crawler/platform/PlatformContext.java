package com.ut.crawler.platform;

import java.util.List;
import java.util.Map;

public class PlatformContext {
    private String name;
    private boolean enabled;
    private boolean requiresLogin;
    private String searchUrlPattern;
    private String scrollSelector;
    private String titleSelector;
    private String authorSelector;
    private Map<String, List<String>> elementsToRemove;
    private String commentSelector;       // ✅ New field
    private String postContentSelector; 
    // Constructors, Getters, Setters

    public String getCommentSelector() {
		return commentSelector;
	}

	public void setCommentSelector(String commentSelector) {
		this.commentSelector = commentSelector;
	}

	public String getPostContentSelector() {
		return postContentSelector;
	}

	public void setPostContentSelector(String postContentSelector) {
		this.postContentSelector = postContentSelector;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isRequiresLogin() {
		return requiresLogin;
	}

	public void setRequiresLogin(boolean requiresLogin) {
		this.requiresLogin = requiresLogin;
	}

	public String getSearchUrlPattern() {
		return searchUrlPattern;
	}

	public void setSearchUrlPattern(String searchUrlPattern) {
		this.searchUrlPattern = searchUrlPattern;
	}

	public String getScrollSelector() {
		return scrollSelector;
	}

	public void setScrollSelector(String scrollSelector) {
		this.scrollSelector = scrollSelector;
	}

	public String getTitleSelector() {
		return titleSelector;
	}

	public void setTitleSelector(String titleSelector) {
		this.titleSelector = titleSelector;
	}

	public String getAuthorSelector() {
		return authorSelector;
	}

	public void setAuthorSelector(String authorSelector) {
		this.authorSelector = authorSelector;
	}

	public Map<String, List<String>> getElementsToRemove() {
		return elementsToRemove;
	}

	public void setElementsToRemove(Map<String, List<String>> elementsToRemove) {
		this.elementsToRemove = elementsToRemove;
	}

	public String buildSearchUrl(String query) {
        return searchUrlPattern.replace("{query}", query.replace(" ", "+"));
    }
}
