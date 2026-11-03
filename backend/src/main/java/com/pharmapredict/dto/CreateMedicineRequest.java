package com.pharmapredict.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public record CreateMedicineRequest(
        @NotBlank @Size(max = 40) String sku,
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 80) String category,
        @NotBlank @Size(max = 120) String manufacturer,
        @NotNull @DecimalMin("0.00") BigDecimal unitPrice,
        @Min(0) int stockQty,
        @Min(0) int reorderLevel,
        @Min(1) @Max(365) int leadTimeDays) {}