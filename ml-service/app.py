from datetime import date, timedelta
from typing import List, Optional
import numpy as np
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error, r2_score
from sklearn.model_selection import train_test_split

app = FastAPI(title="PharmaPredict ML Service", version="1.0.0", description="Medicine demand forecasting using a Random Forest regression model trained on reproducible synthetic demo data.")
model: Optional[RandomForestRegressor] = None
metrics = {"mae": 0.0, "r2": 0.0}


def build_training_data():
    rng = np.random.default_rng(42)
    rows, targets = [], []
    start = date(2024, 1, 1)
    for sku in range(90):
        base = float(rng.uniform(5, 55))
        previous = []
        for offset in range(240):
            day = start + timedelta(days=offset)
            dow = day.weekday()
            month = day.month
            weekday_factor = 1.18 if dow >= 5 else 0.94
            seasonal_factor = 1.0 + 0.16 * np.sin((month / 12.0) * 2 * np.pi)
            demand = max(0.0, base * weekday_factor * seasonal_factor + (previous[-1] * 0.12 if previous else 0) + rng.normal(0, max(1.0, base * 0.18)))
            if len(previous) >= 7:
                rows.append([previous[-1], previous[-7], float(np.mean(previous[-7:])), dow, month, int(dow >= 5)])
                targets.append(demand)
            previous.append(demand)
    return np.asarray(rows, dtype=float), np.asarray(targets, dtype=float)


def train():
    global model, metrics
    x, y = build_training_data()
    x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.2, random_state=42)
    candidate = RandomForestRegressor(n_estimators=90, max_depth=13, min_samples_leaf=2, random_state=42, n_jobs=-1)
    candidate.fit(x_train, y_train)
    predicted = candidate.predict(x_test)
    metrics = {"mae": round(float(mean_absolute_error(y_test, predicted)), 3), "r2": round(float(r2_score(y_test, predicted)), 3)}
    candidate.fit(x, y)
    model = candidate


@app.on_event("startup")
def on_startup():
    train()


class ForecastRequest(BaseModel):
    medicineId: int = Field(gt=0)
    medicineName: str = Field(min_length=1, max_length=160)
    history: List[int] = Field(min_length=7, max_length=90)
    days: int = Field(default=7, ge=1, le=30)
    startDate: date


@app.get("/")
def root():
    return {"service": "PharmaPredict ML Service", "docs": "/docs", "health": "/health"}


@app.get("/health")
def health():
    return {"status": "ok" if model is not None else "starting", "modelReady": model is not None}


@app.get("/model-info")
def model_info():
    return {"model": "RandomForestRegressor", "features": ["lag_1", "lag_7", "rolling_mean_7", "day_of_week", "month", "is_weekend"], "metrics": metrics, "trainingData": "reproducible synthetic demo dataset: 90 product series x 240 days", "warning": "Metrics are from synthetic data and are not evidence of performance on real pharmacy data."}


@app.post("/forecast")
def forecast(request: ForecastRequest):
    if model is None:
        raise HTTPException(status_code=503, detail="Model is still training")
    history = [float(max(0, v)) for v in request.history]
    result = []
    for step in range(request.days):
        forecast_date = request.startDate + timedelta(days=step)
        dow = forecast_date.weekday()
        row = [[history[-1], history[-7], float(np.mean(history[-7:])), dow, forecast_date.month, int(dow >= 5)]]
        value = max(0.0, float(model.predict(row)[0]))
        value = round(value, 1)
        result.append({"date": forecast_date.isoformat(), "predictedDemand": value})
        history.append(value)
    return {
        "medicineId": request.medicineId,
        "medicineName": request.medicineName,
        "model": "RandomForestRegressor",
        "trainingData": "Synthetic demo dataset; replace with validated pharmacy sales data before real use",
        "days": request.days,
        "forecast": result,
        "modelMetrics": metrics
    }
