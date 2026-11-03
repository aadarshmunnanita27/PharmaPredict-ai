export const demoMedicines = [
  { id: 1, sku: 'PCM-500', name: 'Paracetamol 500mg', category: 'Analgesic', manufacturer: 'CareWell Labs', unitPrice: 2.5, stockQty: 84, reorderLevel: 100, leadTimeDays: 3, active: true },
  { id: 2, sku: 'AMX-250', name: 'Amoxicillin 250mg', category: 'Antibiotic', manufacturer: 'Northstar Pharma', unitPrice: 6.8, stockQty: 245, reorderLevel: 80, leadTimeDays: 5, active: true },
  { id: 3, sku: 'CET-10', name: 'Cetirizine 10mg', category: 'Antihistamine', manufacturer: 'MedNova', unitPrice: 1.9, stockQty: 42, reorderLevel: 90, leadTimeDays: 4, active: true },
  { id: 4, sku: 'VIT-D3', name: 'Vitamin D3 60K', category: 'Supplement', manufacturer: 'WellSpring', unitPrice: 14.0, stockQty: 310, reorderLevel: 100, leadTimeDays: 6, active: true },
  { id: 5, sku: 'OMZ-20', name: 'Omeprazole 20mg', category: 'Gastrointestinal', manufacturer: 'CareWell Labs', unitPrice: 4.2, stockQty: 68, reorderLevel: 75, leadTimeDays: 3, active: true },
  { id: 6, sku: 'AZI-500', name: 'Azithromycin 500mg', category: 'Antibiotic', manufacturer: 'Northstar Pharma', unitPrice: 12.5, stockQty: 156, reorderLevel: 60, leadTimeDays: 5, active: true },
  { id: 7, sku: 'ORS-21', name: 'ORS Sachet', category: 'Rehydration', manufacturer: 'HydraCare', unitPrice: 18.0, stockQty: 34, reorderLevel: 70, leadTimeDays: 2, active: true },
  { id: 8, sku: 'MET-500', name: 'Metformin 500mg', category: 'Diabetes Care', manufacturer: 'MedNova', unitPrice: 3.1, stockQty: 520, reorderLevel: 120, leadTimeDays: 4, active: true }
];

const today = new Date();
export const demoTrend = Array.from({ length: 14 }, (_, i) => {
  const d = new Date(today); d.setDate(today.getDate() - 13 + i);
  return { date: d.toISOString().slice(5, 10), unitsSold: 105 + Math.round(25 * Math.sin(i / 2.3) + i * 1.8) };
});
export const demoSummary = { totalMedicines: 8, totalUnitsInStock: 1459, lowStockItems: 4, unitsSoldLast30Days: 3842 };
export const demoAlerts = demoMedicines.filter(m => m.stockQty <= m.reorderLevel);

export function demoForecast(medicine) {
  const base = Math.max(5, Math.round(medicine.stockQty / 12));
  return {
    medicineId: medicine.id,
    medicineName: medicine.name,
    model: 'Demo preview (ML service not connected)',
    trainingData: 'Illustrative values only — not a live model prediction',
    days: 7,
    forecast: Array.from({ length: 7 }, (_, i) => {
      const d = new Date(); d.setDate(d.getDate() + i + 1);
      return { date: d.toISOString().slice(0, 10), predictedDemand: Math.max(1, base + ((i * 3) % 6) - 2) };
    }),
    modelMetrics: { mae: null, r2: null }
  };
}