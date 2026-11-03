package com.pharmapredict.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.pharmapredict.dto.DashboardSummary;
import com.pharmapredict.dto.SalesTrendPoint;
import com.pharmapredict.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService service;
    public DashboardController(DashboardService service) { this.service = service; }
    @GetMapping("/summary") public DashboardSummary summary() { return service.summary(); }
    @GetMapping("/sales-trend") public List<SalesTrendPoint> trend() { return service.trend(); }
}