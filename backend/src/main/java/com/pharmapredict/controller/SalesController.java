package com.pharmapredict.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.pharmapredict.dto.RecordSaleRequest;
import com.pharmapredict.service.SalesService;

@RestController
@RequestMapping("/api/sales")
public class SalesController {
    private final SalesService service;
    public SalesController(SalesService service) { this.service = service; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public void record(@Valid @RequestBody RecordSaleRequest request) { service.record(request); }
}