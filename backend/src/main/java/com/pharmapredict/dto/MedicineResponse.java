package com.pharmapredict.dto;

import java.math.BigDecimal;
import com.pharmapredict.entity.Medicine;

public record MedicineResponse(Long id, String sku, String name, String category, String manufacturer,
                               BigDecimal unitPrice, int stockQty, int reorderLevel, int leadTimeDays, boolean active) {
    public static MedicineResponse from(Medicine m) {
        return new MedicineResponse(m.getId(), m.getSku(), m.getName(), m.getCategory(), m.getManufacturer(),
                m.getUnitPrice(), m.getStockQty(), m.getReorderLevel(), m.getLeadTimeDays(), m.isActive());
    }
}