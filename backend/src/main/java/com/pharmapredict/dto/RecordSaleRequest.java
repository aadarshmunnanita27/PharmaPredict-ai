package com.pharmapredict.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RecordSaleRequest(@NotNull Long medicineId, @Min(1) int quantity, LocalDate soldAt) {}