package com.ut.crawler.models;

import com.ut.crawler.utils.DomainUtils;

public class CrawlUrl {
    private String url;
    private PriorityLevel priority;
    private String platform;
    private int level;
    private TypeOfUrl type;  // enum: SEED or POST
    private Long topicId; 
    public CrawlUrl(String url, PriorityLevel priority, String platform) {
        this.url = url;
        this.priority = priority;
        this.platform = platform;
    }

    public CrawlUrl(String url, PriorityLevel priority, String platform, int level, TypeOfUrl type,Long id) {
        this.url = url;
        this.priority = priority;
        this.platform = platform;
        this.level = level;
        this.type = type;
        this.topicId=id;
    }

    public String getUrl() {
        return url;
    }

    public PriorityLevel getPriority() {
        return priority;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getDomain() {
        return DomainUtils.extractDomain(url).toUpperCase();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public TypeOfUrl getType() {
        return type;
    }

    public void setType(TypeOfUrl type) {
        this.type = type;
    }

	public Long getTopicId() {
		return topicId;
	}

	public void setTopicId(Long topicId) {
		this.topicId = topicId;
	}
}
