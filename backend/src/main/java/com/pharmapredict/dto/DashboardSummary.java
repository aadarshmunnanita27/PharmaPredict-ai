package com.pharmapredict.dto;

public record DashboardSummary(long totalMedicines, long totalUnitsInStock, long lowStockItems, long unitsSoldLast30Days) {}