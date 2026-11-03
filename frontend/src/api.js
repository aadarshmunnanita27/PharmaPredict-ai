const BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '');

async function request(path, options = {}) {
  const response = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }
  });
  if (!response.ok) {
    const body = await response.text();
    throw new Error(body || `API request failed (${response.status})`);
  }
  if (response.status === 204) return null;
  return response.json();
}

export const api = {
  summary: () => request('/api/dashboard/summary'),
  trend: () => request('/api/dashboard/sales-trend'),
  medicines: () => request('/api/medicines'),
  alerts: () => request('/api/medicines/low-stock'),
  forecast: (id, days = 7) => request(`/api/forecasts/${id}?days=${days}`),
  restock: (id, quantity) => request(`/api/medicines/${id}/restock`, { method: 'POST', body: JSON.stringify({ quantity }) }),
  recordSale: (medicineId, quantity, soldAt) => request('/api/sales', { method: 'POST', body: JSON.stringify({ medicineId, quantity, soldAt }) })
};