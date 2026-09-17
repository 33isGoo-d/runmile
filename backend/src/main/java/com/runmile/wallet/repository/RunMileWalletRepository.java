package com.runmile.wallet.repository;

import com.runmile.wallet.domain.RunMileWallet;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RunMileWalletRepository extends JpaRepository<RunMileWallet, Long> {
    Optional<RunMileWallet> findByRunnerId(Long runnerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select wallet from RunMileWallet wallet where wallet.runner.id = :runnerId")
    Optional<RunMileWallet> findByRunnerIdForUpdate(@Param("runnerId") Long runnerId);
}
