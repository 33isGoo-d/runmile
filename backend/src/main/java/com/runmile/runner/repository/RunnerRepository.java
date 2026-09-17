package com.runmile.runner.repository;

import com.runmile.runner.domain.Runner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunnerRepository extends JpaRepository<Runner, Long> {
}
