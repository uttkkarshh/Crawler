package com.ut.crawler.models;

import java.util.List;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Snip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String screenshotPath;

    @OneToMany(mappedBy = "snip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scroll_id")
    private Scroll scroll; 
    // Constructors
    public Snip() {}

    public Scroll getScroll() {
		return scroll;
	}

	public void setScroll(Scroll scroll) {
		this.scroll = scroll;
	}

	public Snip(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }

    // Helper method to add post
    public void addPost(Post post) {
        posts.add(post);
        post.setSnip(this);
    }

    // Helper method to remove post
    public void removePost(Post post) {
        posts.remove(post);
        post.setSnip(null);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getScreenshotPath() { return screenshotPath; }
    public List<Post> getPosts() { return posts; }

    public void setId(Long id) { this.id = id; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
    public void setPosts(List<Post> posts) { this.posts = posts; }
}
