package com.ut.crawler.models;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public void setName(String name) {
		this.name = name;
	}

	private Instant createdAt;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Scroll> scrolls = new ArrayList<>();

    public Topic() {
        this.createdAt = Instant.now();
    }

    public Topic(String name) {
        this.name = name;
        this.createdAt = Instant.now();
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<Scroll> getScrolls() {
        return scrolls;
    }

    public void setScrolls(List<Scroll> scrolls) {
        this.scrolls = scrolls;
    }

    public void addScroll(Scroll scroll) {
        scroll.setTopic(this);
        scrolls.add(scroll);
    }

    public void removeScroll(Scroll scroll) {
        scrolls.remove(scroll);
        scroll.setTopic(null);
    }

    @Override
    public String toString() {
        return "Topic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", scrollCount=" + scrolls.size() +
                '}';
    }
}
