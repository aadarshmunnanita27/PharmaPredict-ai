package com.pharmapredict.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.pharmapredict.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByMedicineIdAndSoldAtBetweenOrderBySoldAtAsc(Long medicineId, LocalDate from, LocalDate to);
    List<Sale> findBySoldAtBetweenOrderBySoldAtAsc(LocalDate from, LocalDate to);
    @Query("select coalesce(sum(s.quantity), 0) from Sale s where s.soldAt >= :from")
    long sumQuantitySince(@Param("from") LocalDate from);
}