# Kabadiwala Connect — backend (today's build)

FastAPI + SQLite. Four endpoints, matching the contract: create a lot (get a price
estimate), match recyclers, confirm a transaction, list transactions for the dashboard.

## Run it

```bash
cd backend
python3 -m venv venv
source venv/bin/activate        # on Windows: venv\Scripts\activate
pip install -r requirements.txt
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

Interactive docs (auto-generated, useful for testing without the Android app yet):
http://localhost:8000/docs

## Quick test with curl

```bash
# 1. Health check
curl http://localhost:8000/health

# 2. See the categories + rates the app should show
curl http://localhost:8000/categories

# 3. Create a lot -> get a price estimate
curl -X POST http://localhost:8000/lots \
  -H "Content-Type: application/json" \
  -d '{"category": "mobile", "weight_kg": 2.5}'
# copy the "id" from the response for the next steps

# 4. Get recycler offers for that lot
curl "http://localhost:8000/recyclers/match?lot_id=PASTE_LOT_ID_HERE"

# 5. Confirm a transaction
curl -X POST http://localhost:8000/transactions \
  -H "Content-Type: application/json" \
  -d '{"lot_id": "PASTE_LOT_ID_HERE", "recycler_name": "EcoRecyclers Pvt Ltd", "amount": 625.0, "payment_method": "cash"}'

# 6. List all transactions (this is what the Streamlit dashboard will show)
curl http://localhost:8000/transactions
```

## Where each core requirement is handled here

- **Offline-tolerant**: this server doesn't need to know or care when the app was
  offline — the app just calls these same endpoints whenever it reconnects.
- **No advanced tooling**: SQLite needs no install/setup step, just this Python file.
- **Small/beginner-friendly**: one file, no ORM, plain `sqlite3` from the standard library.

## Swapping SQLite for MySQL later

Only `get_db()` and `init_db()` touch the database directly — when you're ready to move
to MySQL, replace those two functions (e.g. with `mysql-connector-python`) and the table
schemas stay the same. Nothing in the endpoint functions needs to change.

## For the Android app

Base URL while testing on an emulator: `http://10.0.2.2:8000` (the emulator's alias for
your computer's `localhost`). On a real phone on the same Wi-Fi, use your computer's LAN
IP instead, e.g. `http://192.168.1.23:8000`.
