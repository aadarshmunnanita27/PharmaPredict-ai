package com.pharmapredict;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.pharmapredict.entity.Medicine;

class MedicineStockTest {
    private Medicine medicine() {
        return new Medicine("TST-1", "Test Medicine", "Test", "Test Labs", new BigDecimal("10.00"), 20, 5, 3);
    }

    @Test
    void restockIncreasesAvailableUnits() {
        Medicine medicine = medicine();
        medicine.addStock(15);
        assertEquals(35, medicine.getStockQty());
    }

    @Test
    void saleDecreasesAvailableUnits() {
        Medicine medicine = medicine();
        medicine.removeStock(7);
        assertEquals(13, medicine.getStockQty());
    }

    @Test
    void saleCannotExceedAvailableStock() {
        Medicine medicine = medicine();
        assertThrows(IllegalArgumentException.class, () -> medicine.removeStock(21));
        assertEquals(20, medicine.getStockQty());
    }
}
