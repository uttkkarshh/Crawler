package com.ut.crawler.controller;



import com.ut.crawler.models.Scroll;
import com.ut.crawler.repository.ScrollRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/scrolls")
public class ScrollController {

    private final ScrollRepository scrollRepository;

    public ScrollController(ScrollRepository scrollRepository) {
        this.scrollRepository = scrollRepository;
    }

    // ✅ Get all scrolls
    @GetMapping
    public List<Scroll> getAllScrolls() {
        return scrollRepository.findAll();
    }

    // ✅ Get scroll by ID
    @GetMapping("/{id}")
    public ResponseEntity<Scroll> getScrollById(@PathVariable Long id) {
        Optional<Scroll> scroll = scrollRepository.findById(id);
        return scroll.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Insert a new scroll
    @PostMapping
    public Scroll createScroll(@RequestBody Scroll scroll) {
        return scrollRepository.save(scroll);
    }

    // ✅ Update a scroll
    @PutMapping("/{id}")
    public ResponseEntity<Scroll> updateScroll(@PathVariable Long id, @RequestBody Scroll updatedScroll) {
        return scrollRepository.findById(id)
                .map(scroll -> {
                    scroll.setDescription(updatedScroll.getDescription());
                    scroll.setTopic(updatedScroll.getTopic()); // careful with lazy relation
                    return ResponseEntity.ok(scrollRepository.save(scroll));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Delete scroll
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScroll(@PathVariable Long id) {
        return scrollRepository.findById(id)
                .map(scroll -> {
                    scrollRepository.delete(scroll);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
