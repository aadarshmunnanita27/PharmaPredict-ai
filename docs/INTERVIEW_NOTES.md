# Interview notes — PharmaPredict AI

## Explain the architecture

- React/Vite is the presentation layer and is deployed as a static site.
- Spring Boot is the system-of-record API for medicines, inventory and sales.
- PostgreSQL stores medicines and sales; Flyway controls schema changes.
- FastAPI isolates Python ML dependencies and exposes a prediction endpoint.
- The Java service builds a continuous 30-day daily sales series, including zero-sales days, then calls the ML service.

## Why split Java and Python?

Spring Boot is suitable for transactional APIs and relational persistence. Python has a mature ML ecosystem. Keeping inference behind an HTTP boundary lets each component be built, tested and scaled independently. The trade-off is a network dependency: forecasts can fail when the ML service is unavailable, while normal inventory endpoints remain available.

## Model details

- Algorithm: RandomForestRegressor.
- Features: previous day demand, same weekday one week ago, trailing seven-day mean, day of week, month and weekend flag.
- Forecast horizon: 1–30 days, recursively feeding predictions back as lag values.
- Metrics: MAE and R² on a held-out split of synthetic generated series.
- Limitation: synthetic metrics do not establish accuracy on real pharmacy demand. A real deployment needs historical sales, stockout correction, holidays, promotions, seasonality, backtesting and monitoring.

## Complexity

For one forecast with H days, model inference is H calls to a random forest with T trees and average depth D. Approximate inference work is O(H × T × D); memory depends on the stored trees and the 30-day input history. The Java aggregation reads the selected medicine's sales within a fixed 30-day window; the `(medicine_id, sold_at)` index supports this access path. Dashboard aggregation in this starter implementation scans the requested date window and can be moved to SQL aggregation or precomputed daily totals at higher scale.

## Honest limitations to state

- The seed data and model training data are synthetic.
- The demo does not yet include authentication/RBAC or tenant isolation.
- No message broker or event-driven stock workflow is included.
- Model accuracy must be measured against real, time-ordered data before operational use.
