package com.pharmapredict.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record QuantityRequest(@Min(1) @Max(1000000) int quantity) {}