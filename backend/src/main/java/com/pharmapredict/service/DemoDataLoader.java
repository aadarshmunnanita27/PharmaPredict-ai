package com.pharmapredict.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.pharmapredict.entity.Medicine;
import com.pharmapredict.entity.Sale;
import com.pharmapredict.repository.MedicineRepository;
import com.pharmapredict.repository.SaleRepository;

@Component
public class DemoDataLoader implements CommandLineRunner {
    private final MedicineRepository medicineRepository;
    private final SaleRepository saleRepository;
    public DemoDataLoader(MedicineRepository medicineRepository, SaleRepository saleRepository) { this.medicineRepository = medicineRepository; this.saleRepository = saleRepository; }

    @Override
    @Transactional
    public void run(String... args) {
        if (medicineRepository.count() > 0) return;
        List<Medicine> medicines = medicineRepository.saveAll(List.of(
                new Medicine("PCM-500", "Paracetamol 500mg", "Analgesic", "CareWell Labs", new BigDecimal("2.50"), 84, 100, 3),
                new Medicine("AMX-250", "Amoxicillin 250mg", "Antibiotic", "Northstar Pharma", new BigDecimal("6.80"), 245, 80, 5),
                new Medicine("CET-10", "Cetirizine 10mg", "Antihistamine", "MedNova", new BigDecimal("1.90"), 42, 90, 4),
                new Medicine("VIT-D3", "Vitamin D3 60K", "Supplement", "WellSpring", new BigDecimal("14.00"), 310, 100, 6),
                new Medicine("OMZ-20", "Omeprazole 20mg", "Gastrointestinal", "CareWell Labs", new BigDecimal("4.20"), 68, 75, 3),
                new Medicine("AZI-500", "Azithromycin 500mg", "Antibiotic", "Northstar Pharma", new BigDecimal("12.50"), 156, 60, 5),
                new Medicine("ORS-21", "ORS Sachet", "Rehydration", "HydraCare", new BigDecimal("18.00"), 34, 70, 2),
                new Medicine("MET-500", "Metformin 500mg", "Diabetes Care", "MedNova", new BigDecimal("3.10"), 520, 120, 4)
        ));
        int[] base = {14, 8, 10, 4, 7, 5, 6, 12};
        Random random = new Random(42);
        List<Sale> sales = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < medicines.size(); i++) {
            for (int d = 89; d >= 0; d--) {
                LocalDate date = today.minusDays(d);
                double weekend = date.getDayOfWeek().getValue() >= 6 ? 1.18 : 1.0;
                double seasonal = 1.0 + 0.12 * Math.sin((date.getDayOfYear() / 365.0) * Math.PI * 2);
                int qty = Math.max(0, (int) Math.round(base[i] * weekend * seasonal + random.nextGaussian() * Math.max(1.2, base[i] * 0.22)));
                if (qty > 0) sales.add(new Sale(medicines.get(i), qty, date));
            }
        }
        saleRepository.saveAll(sales);
    }
}