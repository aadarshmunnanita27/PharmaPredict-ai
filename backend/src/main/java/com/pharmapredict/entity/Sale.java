package com.pharmapredict.entity;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "sales")
public class Sale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;
    @Column(nullable = false)
    private int quantity;
    @Column(name = "sold_at", nullable = false)
    private LocalDate soldAt;

    protected Sale() {}
    public Sale(Medicine medicine, int quantity, LocalDate soldAt) { this.medicine = medicine; this.quantity = quantity; this.soldAt = soldAt; }
    public Long getId() { return id; }
    public Medicine getMedicine() { return medicine; }
    public int getQuantity() { return quantity; }
    public LocalDate getSoldAt() { return soldAt; }
}