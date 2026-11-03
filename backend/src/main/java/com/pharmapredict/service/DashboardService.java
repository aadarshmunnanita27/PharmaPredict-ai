package com.pharmapredict.service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.pharmapredict.dto.DashboardSummary;
import com.pharmapredict.dto.SalesTrendPoint;
import com.pharmapredict.entity.Sale;
import com.pharmapredict.repository.MedicineRepository;
import com.pharmapredict.repository.SaleRepository;

@Service
public class DashboardService {
    private final MedicineRepository medicineRepository;
    private final SaleRepository saleRepository;
    public DashboardService(MedicineRepository medicineRepository, SaleRepository saleRepository) { this.medicineRepository = medicineRepository; this.saleRepository = saleRepository; }

    public DashboardSummary summary() {
        long stock = medicineRepository.findByActiveTrueOrderByNameAsc().stream().mapToLong(m -> m.getStockQty()).sum();
        return new DashboardSummary(medicineRepository.countByActiveTrue(), stock,
                medicineRepository.findByActiveTrueOrderByNameAsc().stream().filter(m -> m.getStockQty() <= m.getReorderLevel()).count(),
                saleRepository.sumQuantitySince(LocalDate.now().minusDays(29)));
    }

    public List<SalesTrendPoint> trend() {
        LocalDate today = LocalDate.now(); LocalDate from = today.minusDays(13);
        Map<LocalDate, Long> totals = saleRepository.findBySoldAtBetweenOrderBySoldAtAsc(from, today).stream()
                .collect(Collectors.groupingBy(Sale::getSoldAt, Collectors.summingLong(Sale::getQuantity)));
        List<SalesTrendPoint> result = new ArrayList<>();
        for (LocalDate day = from; !day.isAfter(today); day = day.plusDays(1)) result.add(new SalesTrendPoint(day, totals.getOrDefault(day, 0L)));
        return result;
    }
}