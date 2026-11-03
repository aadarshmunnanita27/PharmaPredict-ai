package com.pharmapredict.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.pharmapredict.entity.Medicine;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByActiveTrueOrderByNameAsc();
    @Query("select m from Medicine m where m.active = true and m.stockQty <= m.reorderLevel order by m.stockQty asc")
    List<Medicine> findLowStock();
    long countByActiveTrue();
}
