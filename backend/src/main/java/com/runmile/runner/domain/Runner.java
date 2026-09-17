package com.runmile.runner.domain;

import com.runmile.global.type.Course;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "runner")
public class Runner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "runner_code", nullable = false, unique = true, length = 64)
    private String runnerCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Course course;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    protected Runner() {
    }

    public Long getId() {
        return id;
    }

    public String getRunnerCode() {
        return runnerCode;
    }

    public Course getCourse() {
        return course;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
