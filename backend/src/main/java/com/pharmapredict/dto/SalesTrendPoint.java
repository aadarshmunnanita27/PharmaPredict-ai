package com.pharmapredict.dto;

import java.time.LocalDate;
public record SalesTrendPoint(LocalDate date, long unitsSold) {}