package com.pharmapredict.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.pharmapredict.dto.CreateMedicineRequest;
import com.pharmapredict.dto.MedicineResponse;
import com.pharmapredict.dto.QuantityRequest;
import com.pharmapredict.service.MedicineService;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {
    private final MedicineService service;
    public MedicineController(MedicineService service) { this.service = service; }
    @GetMapping public List<MedicineResponse> findAll() { return service.findAll(); }
    @GetMapping("/low-stock") public List<MedicineResponse> lowStock() { return service.findLowStock(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public MedicineResponse create(@Valid @RequestBody CreateMedicineRequest request) { return service.create(request); }
    @PostMapping("/{id}/restock") public MedicineResponse restock(@PathVariable Long id, @Valid @RequestBody QuantityRequest request) { return service.restock(id, request.quantity()); }
}