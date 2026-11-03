package com.pharmapredict.dto;

import java.time.LocalDate;
import java.util.List;
public record ForecastRequest(Long medicineId, String medicineName, List<Integer> history, int days, LocalDate startDate) {}