package com.pharmapredict.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import com.pharmapredict.dto.CreateMedicineRequest;
import com.pharmapredict.dto.MedicineResponse;
import com.pharmapredict.entity.Medicine;
import com.pharmapredict.repository.MedicineRepository;

@Service
public class MedicineService {
    private final MedicineRepository repository;
    public MedicineService(MedicineRepository repository) { this.repository = repository; }

    public List<MedicineResponse> findAll() { return repository.findByActiveTrueOrderByNameAsc().stream().map(MedicineResponse::from).toList(); }
    public List<MedicineResponse> findLowStock() { return repository.findLowStock().stream().map(MedicineResponse::from).toList(); }
    public Medicine findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine not found: " + id)); }

    @Transactional
    public MedicineResponse create(CreateMedicineRequest request) {
        Medicine m = new Medicine(request.sku(), request.name(), request.category(), request.manufacturer(), request.unitPrice(), request.stockQty(), request.reorderLevel(), request.leadTimeDays());
        return MedicineResponse.from(repository.save(m));
    }

    @Transactional
    public MedicineResponse restock(Long id, int quantity) {
        Medicine m = findEntity(id); m.addStock(quantity); return MedicineResponse.from(m);
    }
}