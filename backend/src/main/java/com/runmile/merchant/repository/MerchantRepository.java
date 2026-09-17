package com.runmile.merchant.repository;

import com.runmile.global.type.MerchantCategory;
import com.runmile.merchant.domain.Merchant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    @Query("""
            select merchant from Merchant merchant
            where (:district is null or merchant.district = :district)
              and (:category is null or merchant.category = :category)
              and (:runmileEnabled is null or merchant.runmileEnabled = :runmileEnabled)
            order by merchant.id
            """)
    List<Merchant> findByFilters(
            @Param("district") String district,
            @Param("category") MerchantCategory category,
            @Param("runmileEnabled") Boolean runmileEnabled
    );
}
