package com.pharmapredict.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pharmapredict.dto.RecordSaleRequest;
import com.pharmapredict.entity.Medicine;
import com.pharmapredict.entity.Sale;
import com.pharmapredict.repository.SaleRepository;

@Service
public class SalesService {
    private final MedicineService medicineService;
    private final SaleRepository saleRepository;
    public SalesService(MedicineService medicineService, SaleRepository saleRepository) { this.medicineService = medicineService; this.saleRepository = saleRepository; }

    @Transactional
    public void record(RecordSaleRequest request) {
        Medicine medicine = medicineService.findEntity(request.medicineId());
        medicine.removeStock(request.quantity());
        saleRepository.save(new Sale(medicine, request.quantity(), request.soldAt() == null ? LocalDate.now() : request.soldAt()));
    }
}