package com.runmile.completion.repository;

import com.runmile.completion.domain.NftRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NftRecordRepository extends JpaRepository<NftRecord, Long> {
    @Override
    @EntityGraph(attributePaths = {"runner", "completion"})
    List<NftRecord> findAll();

    @EntityGraph(attributePaths = {"runner", "completion"})
    Optional<NftRecord> findByRunnerId(Long runnerId);
}
