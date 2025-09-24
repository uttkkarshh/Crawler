package com.ut.crawler.models;



import jakarta.persistence.*;

@Entity
@Table(name = "seeds")
public class Seed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;       // or the search keyword
    private String platform;    // e.g. YOUTUBE, REDDIT

    public Seed() {}

    public Seed(String topic, String platform) {
        this.topic = topic;
        this.platform = platform;
    }

    public Long getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @Override
    public String toString() {
        return "Seed{id=" + id + ", topic='" + topic + "', platform='" + platform + "'}";
    }
}
