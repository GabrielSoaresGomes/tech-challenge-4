package com.postech.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
public class Feedback extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String description;

    @Column(nullable = false)
    public Integer note;

    @Column(nullable = false)
    public Boolean urgency = false;

    @Column(name = "send_date", nullable = false)
    public LocalDateTime sendDate;
}
