package com.postech.repository;

import com.postech.domain.Feedback;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class FeedbackRepository implements PanacheRepository<Feedback> {

    public List<Feedback> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return find("sendDate BETWEEN ?1 AND ?2", start, end).list();
    }
}
