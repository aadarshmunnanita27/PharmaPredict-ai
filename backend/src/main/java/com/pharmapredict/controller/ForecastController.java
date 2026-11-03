package com.pharmapredict.controller;

import org.springframework.web.bind.annotation.*;
import com.pharmapredict.dto.MlForecastResponse;
import com.pharmapredict.service.ForecastService;

@RestController
@RequestMapping("/api/forecasts")
public class ForecastController {
    private final ForecastService service;
    public ForecastController(ForecastService service) { this.service = service; }
    @GetMapping("/{medicineId}") public MlForecastResponse forecast(@PathVariable Long medicineId, @RequestParam(defaultValue = "7") int days) { return service.forecast(medicineId, days); }
}