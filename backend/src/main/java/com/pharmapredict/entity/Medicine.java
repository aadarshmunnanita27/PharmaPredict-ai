package com.pharmapredict.entity;

import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
@Table(name = "medicines")
public class Medicine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 40)
    private String sku;
    @Column(nullable = false, length = 160)
    private String name;
    @Column(nullable = false, length = 80)
    private String category;
    @Column(nullable = false, length = 120)
    private String manufacturer;
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;
    @Column(name = "stock_qty", nullable = false)
    private int stockQty;
    @Column(name = "reorder_level", nullable = false)
    private int reorderLevel;
    @Column(name = "lead_time_days", nullable = false)
    private int leadTimeDays;
    @Column(nullable = false)
    private boolean active = true;

    protected Medicine() {}

    public Medicine(String sku, String name, String category, String manufacturer, BigDecimal unitPrice, int stockQty, int reorderLevel, int leadTimeDays) {
        this.sku = sku; this.name = name; this.category = category; this.manufacturer = manufacturer;
        this.unitPrice = unitPrice; this.stockQty = stockQty; this.reorderLevel = reorderLevel; this.leadTimeDays = leadTimeDays;
    }

    public void addStock(int quantity) { this.stockQty += quantity; }
    public void removeStock(int quantity) {
        if (quantity > stockQty) throw new IllegalArgumentException("Sale quantity exceeds available stock");
        this.stockQty -= quantity;
    }
    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getManufacturer() { return manufacturer; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getStockQty() { return stockQty; }
    public int getReorderLevel() { return reorderLevel; }
    public int getLeadTimeDays() { return leadTimeDays; }
    public boolean isActive() { return active; }
}