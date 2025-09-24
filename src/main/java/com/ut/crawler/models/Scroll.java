package com.ut.crawler.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class Scroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;
    


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    
    @OneToMany(mappedBy = "scroll", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Snip> snips = new ArrayList<>();
    // Getters and setters

    public List<Snip> getSnips() {
		return snips;
	}

	public void setSnips(List<Snip> snips) {
		this.snips = snips;
	}

	public Long getId() {
        return id;
    }

  

 
    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    @Override
    public String toString() {
        return "Scroll{" +
                "id=" + id +
            
                '}';
    }

	public void addSnip(Snip snip) {
		// TODO Auto-generated method stub
		this.snips.add(snip);
	}

	public void merge(Scroll other) {
	    if (other == null || other.getSnips() == null) return;

	    for (Snip snip : other.getSnips()) {
	        // Detach the snip from the old Scroll (if needed)
	        snip.setScroll(this); // Reassign parent Scroll
	        this.snips.add(snip);
	    }
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}

