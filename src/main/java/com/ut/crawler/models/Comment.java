package com.ut.crawler.models;

import jakarta.persistence.*;

@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String screenshotPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    // Constructors
    public Comment() {}

    public Comment(String screenshotPath, Post post) {
        this.screenshotPath = screenshotPath;
        this.post = post;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }

    public void setScreenshotPath(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", screenshotPath='" + screenshotPath + '\'' +
                '}';
    }
}
