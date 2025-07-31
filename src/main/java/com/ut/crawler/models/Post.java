package com.ut.crawler.models;

import jakarta.persistence.*;

@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String url;
    private String author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snip_id")
    private Snip snip;

    // Constructors
    public Post() {}

    public Post(String title, String url, String author) {
        this.title = title;
        this.url = url;
        this.author = author;
  
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public String getAuthor() { return author; }
    public Snip getSnip() { return snip; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setUrl(String url) { this.url = url; }
    public void setAuthor(String author) { this.author = author; }
    public void setSnip(Snip snip) { this.snip = snip; }
}

