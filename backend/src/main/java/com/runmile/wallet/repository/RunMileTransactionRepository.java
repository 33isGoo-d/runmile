package com.runmile.wallet.repository;

import com.runmile.global.type.RunMileTransactionType;
import com.runmile.wallet.domain.RunMileTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunMileTransactionRepository extends JpaRepository<RunMileTransaction, Long> {
    boolean existsByWalletIdAndType(Long walletId, RunMileTransactionType type);

    List<RunMileTransaction> findAllByWalletRunnerIdOrderByCreatedAtDesc(Long runnerId);
}
