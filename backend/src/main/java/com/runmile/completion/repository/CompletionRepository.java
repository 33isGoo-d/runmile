package com.runmile.completion.repository;

import com.runmile.completion.domain.Completion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletionRepository extends JpaRepository<Completion, Long> {
    Optional<Completion> findByRunnerId(Long runnerId);
}
