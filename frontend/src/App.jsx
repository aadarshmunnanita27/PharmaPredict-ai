import { useEffect, useMemo, useState } from 'react';
import { Activity, AlertTriangle, ArrowDownRight, ArrowUpRight, Boxes, ChartNoAxesCombined, ChevronDown, CircleHelp, Clock3, Download, LayoutDashboard, Package, Plus, RefreshCw, Search, ShoppingCart, Sparkles, Stethoscope, Truck, X } from 'lucide-react';
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { api } from './api';
import { demoAlerts, demoForecast, demoMedicines, demoSummary, demoTrend } from './demo';

const money = value => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(value || 0);
const dateLabel = value => new Date(`${value}T00:00:00`).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' });

function StatCard({ icon: Icon, label, value, note, tone = 'blue', trend }) {
  return <div className="stat-card">
    <div className={`stat-icon ${tone}`}><Icon size={19} /></div>
    <div className="stat-label">{label}</div>
    <div className="stat-value">{value}</div>
    <div className="stat-note">{trend && <span className={trend === 'up' ? 'positive' : 'negative'}>{trend === 'up' ? <ArrowUpRight size={13}/> : <ArrowDownRight size={13}/>}</span>}{note}</div>
  </div>;
}

function App() {
  const [medicines, setMedicines] = useState(demoMedicines);
  const [summary, setSummary] = useState(demoSummary);
  const [trend, setTrend] = useState(demoTrend);
  const [alerts, setAlerts] = useState(demoAlerts);
  const [live, setLive] = useState(false);
  const [loading, setLoading] = useState(true);
  const [selectedId, setSelectedId] = useState(1);
  const [forecast, setForecast] = useState(null);
  const [forecastBusy, setForecastBusy] = useState(false);
  const [query, setQuery] = useState('');
  const [notice, setNotice] = useState('');
  const [showRestock, setShowRestock] = useState(null);
  const [restockQty, setRestockQty] = useState('50');
  const [days, setDays] = useState(7);

  const selected = medicines.find(m => m.id === Number(selectedId)) || medicines[0];
  const filtered = useMemo(() => medicines.filter(m => `${m.name} ${m.sku} ${m.category}`.toLowerCase().includes(query.toLowerCase())), [medicines, query]);

  async function loadData() {
    setLoading(true);
    try {
      const [s, t, m, a] = await Promise.all([api.summary(), api.trend(), api.medicines(), api.alerts()]);
      setSummary(s); setTrend(t); setMedicines(m); setAlerts(a); setLive(true);
      setSelectedId(current => m.some(x => x.id === current) ? current : (m[0]?.id ?? 1));
    } catch {
      setLive(false);
    } finally { setLoading(false); }
  }

  useEffect(() => { loadData(); }, []);
  useEffect(() => {
    if (!selected) return;
    let cancelled = false;
    setForecastBusy(true);
    const run = live ? api.forecast(selected.id, days) : Promise.resolve(demoForecast(selected));
    run.then(result => { if (!cancelled) setForecast(result); })
      .catch(() => { if (!cancelled) setForecast(demoForecast(selected)); })
      .finally(() => { if (!cancelled) setForecastBusy(false); });
    return () => { cancelled = true; };
  }, [selectedId, live, days, medicines]);

  async function submitRestock() {
    const qty = Number(restockQty);
    if (!Number.isInteger(qty) || qty < 1) { setNotice('Enter a whole-number quantity greater than zero.'); return; }
    try {
      await api.restock(showRestock.id, qty);
      setNotice(`Added ${qty} units to ${showRestock.name}.`);
      setShowRestock(null); await loadData();
    } catch { setNotice('Restock requires a connected backend. Start or deploy the Spring Boot API first.'); }
  }

  async function exportInventory() {
    const rows = [['SKU','Medicine','Category','Stock','Reorder level','Unit price'], ...medicines.map(m => [m.sku,m.name,m.category,m.stockQty,m.reorderLevel,m.unitPrice])];
    const csv = rows.map(row => row.map(v => `"${String(v ?? '').replaceAll('"','""')}"`).join(',')).join('\n');
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv' }));
    const a = document.createElement('a'); a.href = url; a.download = 'pharmapredict-inventory.csv'; a.click(); URL.revokeObjectURL(url);
  }

  const predictedTotal = forecast?.forecast?.reduce((s, p) => s + Number(p.predictedDemand || 0), 0) || 0;
  const selectedRisk = selected && selected.stockQty <= selected.reorderLevel;

  return <div className="app-shell">
    <aside className="sidebar">
      <div className="brand"><div className="brand-mark"><Stethoscope size={21}/></div><div><div className="brand-name">pharma<span>predict</span></div><div className="brand-sub">INVENTORY INTELLIGENCE</div></div></div>
      <div className="side-label">WORKSPACE</div>
      <button className="nav-item active"><LayoutDashboard size={17}/> Overview</button>
      <a className="nav-item" href="#inventory"><Package size={17}/> Inventory <span className="nav-count">{medicines.length}</span></a>
      <a className="nav-item" href="#forecast"><ChartNoAxesCombined size={17}/> Demand forecast</a>
      <a className="nav-item" href="#alerts"><AlertTriangle size={17}/> Stock alerts <span className="nav-count warn">{alerts.length}</span></a>
      <div className="sidebar-bottom"><div className="help-card"><div className="help-icon"><CircleHelp size={17}/></div><b>Need a hand?</b><p>Explore the README for setup, API and deployment instructions.</p><a href="https://github.com" target="_blank" rel="noreferrer">Project documentation ↗</a></div><div className="profile"><div className="avatar">PM</div><div><b>Pharmacy Manager</b><span>Demo workspace</span></div><ChevronDown size={15}/></div></div>
    </aside>

    <main className="main-content">
      <header className="topbar"><div className="breadcrumb">Workspace <span>/</span> <b>Overview</b></div><div className="top-actions"><div className={`connection ${live ? 'connected' : 'demo'}`}><span/> {live ? 'Live data connected' : 'Demo preview'}</div><button className="icon-btn" title="Refresh data" onClick={loadData}><RefreshCw size={17}/></button><div className="top-avatar">PM</div></div></header>

      <div className="page-wrap">
        {!live && <div className="demo-banner"><Sparkles size={16}/><span><b>Preview mode:</b> showing illustrative sample data. Connect the Spring Boot API and ML service to enable live inventory and model predictions.</span></div>}
        {notice && <div className="notice"><span>{notice}</span><button onClick={() => setNotice('')}><X size={15}/></button></div>}

        <section className="page-heading"><div><div className="eyebrow">TUESDAY, {new Date().toLocaleDateString('en-IN', { day: '2-digit', month: 'long', year: 'numeric' }).toUpperCase()}</div><h1>Inventory overview</h1><p>Monitor stock health and make smarter replenishment decisions.</p></div><button className="secondary-btn" onClick={exportInventory}><Download size={16}/> Export inventory</button></section>

        <section className="stats-grid">
          <StatCard icon={Package} label="Active medicines" value={summary.totalMedicines ?? 0} note="Across your catalogue" tone="blue" />
          <StatCard icon={Boxes} label="Units in stock" value={Number(summary.totalUnitsInStock || 0).toLocaleString('en-IN')} note="Current inventory" tone="violet" />
          <StatCard icon={AlertTriangle} label="Low stock items" value={summary.lowStockItems ?? alerts.length} note="Need replenishment" tone="orange" />
          <StatCard icon={ShoppingCart} label="Units sold · 30 days" value={Number(summary.unitsSoldLast30Days || 0).toLocaleString('en-IN')} note="Recent sales volume" tone="green" trend="up" />
        </section>

        <section className="content-grid">
          <div className="panel sales-panel"><div className="panel-head"><div><h2>Sales activity</h2><p>Daily units sold across all medicines</p></div><div className="period-chip"><Activity size={14}/> Last 14 days</div></div>
            <div className="chart-summary"><div><span className="muted-label">TOTAL UNITS</span><strong>{trend.reduce((s, d) => s + Number(d.unitsSold || 0), 0).toLocaleString('en-IN')}</strong></div><div className="chart-legend"><span/> Units sold</div></div>
            <div className="chart-wrap"><ResponsiveContainer width="100%" height="100%"><AreaChart data={trend} margin={{ top: 10, right: 5, left: -18, bottom: 0 }}><defs><linearGradient id="salesFill" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#4f73e8" stopOpacity={0.2}/><stop offset="100%" stopColor="#4f73e8" stopOpacity={0.01}/></linearGradient></defs><CartesianGrid stroke="#edf0f6" vertical={false}/><XAxis dataKey="date" tickFormatter={v => v.length > 5 ? dateLabel(v) : v} tickLine={false} axisLine={false} tick={{ fill: '#9299aa', fontSize: 11 }}/><YAxis tickLine={false} axisLine={false} tick={{ fill: '#9299aa', fontSize: 11 }}/><Tooltip contentStyle={{ border: '1px solid #e9ecf3', borderRadius: 12, boxShadow: '0 8px 24px #18234a12' }} formatter={v => [`${v} units`, 'Sales']}/><Area type="monotone" dataKey="unitsSold" stroke="#5273df" strokeWidth={2.5} fill="url(#salesFill)" activeDot={{ r: 5, strokeWidth: 0 }}/></AreaChart></ResponsiveContainer></div>
          </div>

          <div className="panel alerts-panel" id="alerts"><div className="panel-head"><div><h2>Needs attention</h2><p>Items at or below reorder level</p></div><span className="alert-count">{alerts.length} alerts</span></div>
            {alerts.length ? <div className="alert-list">{alerts.slice(0, 5).map(m => <div className="alert-row" key={m.id}><div className="medicine-symbol">{m.name.slice(0,1)}</div><div className="alert-info"><b>{m.name}</b><span>{m.sku} · Reorder at {m.reorderLevel}</span></div><div className="stock-pill">{m.stockQty} left</div></div>)}</div> : <div className="empty-state"><div className="empty-icon"><Package size={20}/></div><b>Stock levels look healthy</b><span>No medicines need attention right now.</span></div>}
            <a className="text-link" href="#inventory">View inventory <span>→</span></a>
          </div>
        </section>

        <section className="panel forecast-panel" id="forecast"><div className="panel-head forecast-head"><div><div className="section-kicker"><Sparkles size={14}/> MACHINE LEARNING</div><h2>Medicine demand forecast</h2><p>Estimate upcoming demand to plan stock replenishment.</p></div><div className="forecast-controls"><select value={selectedId} onChange={e => setSelectedId(Number(e.target.value))}>{medicines.map(m => <option value={m.id} key={m.id}>{m.name}</option>)}</select><select value={days} onChange={e => setDays(Number(e.target.value))}><option value={7}>Next 7 days</option><option value={14}>Next 14 days</option></select></div></div>
          {selected && <div className="forecast-content"><div className="forecast-kpis"><div className="forecast-kpi"><span className="muted-label">PREDICTED DEMAND</span><strong>{Math.round(predictedTotal)} <small>units</small></strong><span className="kpi-caption">Next {days} days</span></div><div className="forecast-kpi"><span className="muted-label">CURRENT STOCK</span><strong>{selected.stockQty} <small>units</small></strong><span className={`kpi-caption ${selectedRisk ? 'risk-text' : 'ok-text'}`}>{selectedRisk ? 'Below reorder level' : 'Above reorder level'}</span></div><div className="forecast-kpi"><span className="muted-label">REORDER LEVEL</span><strong>{selected.reorderLevel} <small>units</small></strong><span className="kpi-caption">Lead time: {selected.leadTimeDays} days</span></div></div>
            <div className="forecast-visual"><div className="forecast-visual-head"><b>Daily predicted demand</b><span>{forecast?.model || 'Loading model…'}</span></div>{forecastBusy ? <div className="forecast-loading"><RefreshCw size={18}/> Preparing forecast…</div> : <div className="forecast-bars">{(forecast?.forecast || []).map((p, i) => { const max = Math.max(1, ...forecast.forecast.map(x => Number(x.predictedDemand || 0))); return <div className="forecast-bar-col" key={p.date}><div className="bar-value">{Math.round(p.predictedDemand)}</div><div className="bar-track"><div className="bar-fill" style={{ height: `${Math.max(5, Number(p.predictedDemand) / max * 100)}%` }}/></div><span>{dateLabel(p.date)}</span></div>; })}</div>}
              <div className="forecast-foot"><span><Clock3 size={13}/> {live ? 'Forecast generated by ML service' : 'Illustrative preview — not a model prediction'}</span>{live && forecast?.modelMetrics?.mae != null && <span>Validation MAE: {Number(forecast.modelMetrics.mae).toFixed(2)}</span>}</div>
            </div></div>}
        </section>

        <section className="panel inventory-panel" id="inventory"><div className="panel-head inventory-head"><div><h2>Medicine inventory</h2><p>Manage medicine stock and replenishment.</p></div><div className="inventory-actions"><div className="search-box"><Search size={15}/><input value={query} onChange={e => setQuery(e.target.value)} placeholder="Search medicines..."/></div><span className="table-count">{filtered.length} medicines</span></div></div>
          <div className="table-scroll"><table><thead><tr><th>MEDICINE</th><th>CATEGORY</th><th>UNIT PRICE</th><th>AVAILABLE STOCK</th><th>REORDER LEVEL</th><th>STATUS</th><th></th></tr></thead><tbody>{filtered.map(m => { const low = m.stockQty <= m.reorderLevel; return <tr key={m.id}><td><div className="medicine-cell"><div className="medicine-symbol">{m.name.slice(0,1)}</div><div><b>{m.name}</b><span>{m.sku}</span></div></div></td><td>{m.category}</td><td>{money(m.unitPrice)}</td><td><b>{m.stockQty}</b> <span className="unit-text">units</span></td><td>{m.reorderLevel} units</td><td><span className={`status ${low ? 'low' : 'healthy'}`}><i/>{low ? 'Low stock' : 'Healthy'}</span></td><td><button className="restock-btn" onClick={() => { setShowRestock(m); setRestockQty('50'); }}>Restock</button></td></tr>; })}</tbody></table>{!filtered.length && <div className="empty-table">No medicines match your search.</div>}</div>
        </section>
        <footer className="footer"><span>PharmaPredict AI <span className="footer-dot">·</span> Portfolio project</span><span>Forecasts are decision-support estimates, not medical advice.</span></footer>
      </div>
    </main>
    {showRestock && <div className="modal-backdrop" onClick={() => setShowRestock(null)}><div className="modal" onClick={e => e.stopPropagation()}><button className="modal-close" onClick={() => setShowRestock(null)}><X size={18}/></button><div className="modal-icon"><Truck size={21}/></div><h3>Restock medicine</h3><p>Add inventory units for <b>{showRestock.name}</b>.</p><label>Quantity to add</label><input type="number" min="1" value={restockQty} onChange={e => setRestockQty(e.target.value)}/><div className="modal-actions"><button className="secondary-btn" onClick={() => setShowRestock(null)}>Cancel</button><button className="primary-btn" onClick={submitRestock}><Plus size={15}/> Add stock</button></div></div></div>}
  </div>;
}

export default App;