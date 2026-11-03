package com.pharmapredict.dto;

import java.time.LocalDate;
import java.util.List;
public record MlForecastResponse(Long medicineId, String medicineName, String model, String trainingData, int days,
                                 List<ForecastPoint> forecast, ModelMetrics modelMetrics) {
    public record ForecastPoint(LocalDate date, double predictedDemand) {}
    public record ModelMetrics(double mae, double r2) {}
}