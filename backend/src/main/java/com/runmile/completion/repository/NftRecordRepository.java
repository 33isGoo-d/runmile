package com.runmile.completion.repository;

import com.runmile.completion.domain.NftRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NftRecordRepository extends JpaRepository<NftRecord, Long> {
    Optional<NftRecord> findByRunnerId(Long runnerId);
}
