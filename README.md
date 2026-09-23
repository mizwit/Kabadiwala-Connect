# Kabadiwala Connect - Working Prototype

A vernacular, low-literacy, offline-tolerant mobile platform that enables informal scrap collectors to discover fair prices, connect directly with authorized recyclers, complete documented and traceable material handovers, and receive payment.

## Quick Start

### Backend (FastAPI + SQLite)
```bash
cd backend
python -m venv venv
# On Windows: venv\Scripts\activate
# On Mac/Linux: source venv/bin/activate
pip install -r requirements.txt
python main.py
```

The backend will be available at `http://0.0.0.0:8000` with interactive docs at `http://localhost:8000/docs`

### Dashboard (Streamlit)
```bash
cd dashboard
pip install -r requirements.txt
streamlit run app.py --server.headless true
```

The dashboard will be available at `http://localhost:8501`

### Android App
```bash
cd android
./gradlew.bat assembleDebug  # Windows
# OR
./gradlew assembleDebug      # Mac/Linux
```

Install the APK: `android/app/build/outputs/apk/debug/app-debug.apk`


## Architecture Overview

### Backend API (FastAPI)
- **Database**: SQLite with authentication tables
- **Authentication**: OTP-based registration with PIN login
- **Endpoints**:
  - `GET /health` - Health check
  - `GET /categories` - Get material categories with rates
  - `POST /lots` - Create material lot with price estimate
  - `GET /recyclers/match?lot_id={id}` - Match recyclers for a lot
  - `POST /transactions` - Confirm transaction
  - `GET /transactions` - List all transactions
  - `POST /auth/register` - Request OTP for registration
  - `POST /auth/verify-otp` - Verify OTP code
  - `POST /auth/complete-registration` - Complete registration with name/PIN
  - `POST /auth/login` - Login with PIN hash

### Android App (Native Java)
- **Authentication**: Mobile number + 4-digit PIN system
- **Offline-first**: Room DB (SQLite) for local storage
- **Security**: SHA-256 PIN hashing with device ID salt
- **Network**: Multi-URL fallback (LAN, emulator, localhost)
- **UI**: Registration flow, PIN login, lot creation, recycler selection, transaction confirmation
- **Entities**: Collector, Material, Recycler, MaterialLot, Transaction

### Dashboard (Streamlit)
- **Features**: Transaction listing, material categories with rates, health check
- **Real-time**: Connects to FastAPI backend for live data

## Authentication Flow

1. **Registration**:
   - User enters mobile number
   - Backend generates 4-digit OTP (shown in response for prototype)
   - User verifies OTP
   - User sets 4-digit PIN and name
   - Backend generates auth token (stored locally encrypted)

2. **Login**:
   - User enters 4-digit PIN
   - App verifies PIN against local hash (offline-capable)
   - When online, app refreshes auth token from backend
   - All API calls include Bearer token authorization

## Testing the API

```bash
# Health check
curl http://localhost:8000/health

# Get categories
curl http://localhost:8000/categories

# Request OTP
curl -X POST http://localhost:8000/auth/register \
  -H "Content-Type: application/json" \
  -d '{"phone":"9876543210","name":"","device_id":"test_device"}'

# Verify OTP
curl -X POST http://localhost:8000/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"phone":"9876543210","otp":"1234","device_id":"test_device"}'

# Complete registration
curl -X POST http://localhost:8000/auth/complete-registration \
  -H "Content-Type: application/json" \
  -d '{"phone":"9876543210","name":"Test User","device_id":"test_device"}'

# Create a lot (authenticated)
curl -X POST http://localhost:8000/lots \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"category": "Copper Cables", "weight_kg": 2.5}'

# Match recyclers
curl "http://localhost:8000/recyclers/match?lot_id={lot_id}"

# Confirm transaction
curl -X POST http://localhost:8000/transactions \
  -H "Content-Type: application/json" \
  -d '{"lot_id": "{lot_id}", "recycler_name": "EcoRecyclers Pvt Ltd", "amount": 625.0, "payment_method": "cash"}'

# List transactions
curl http://localhost:8000/transactions
```

## Database Schema

The database implements the ERD with the following tables:
- **collectors**: Collector profiles with authentication data
- **materials**: Material categories and types
- **recyclers**: Authorized recyclers with contact info
- **material_lots**: Digital lots of collected materials
- **lot_materials**: Many-to-many relationship between lots and materials
- **transactions**: Transaction records with pricing
- **payments**: Payment tracking and status
- **otp_storage**: Temporary OTP storage for authentication
- **auth_tokens**: Long-lived authentication tokens

## Technology Stack

- **Backend**: FastAPI, SQLite, Python 3.13
- **Android**: Native Java, Room DB, Retrofit, OkHttp, Gradle 8.5
- **Dashboard**: Streamlit, Pandas, Requests
- **Security**: SHA-256 hashing, device ID salt, encrypted storage
- **AI/ML**: TensorFlow Lite (planned for image classification)

## End-to-End Flow

1. **Collector** registers with mobile number and sets 4-digit PIN
2. **Collector** logs in offline using PIN
3. **Collector** selects material category and enters weight
4. **App** calculates estimated value using local rates
5. **App** creates digital lot in local Room DB
6. **App** syncs lot to backend when online (with auth token)
7. **Backend** matches lot with authorized recyclers
8. **Collector** selects recycler and confirms handover
9. **Backend** records transaction and payment
10. **Dashboard** shows real-time transaction data
11. **App** updates local DB with transaction status

## Development Notes

- **Gradle Version**: 8.5 (compatible with Java 21)
- **Android SDK**: 34
- **Min SDK**: 21 (Android 5.0+)
- **Target SDK**: 34 (Android 14)
- **Java Version**: 17 (compile/target)

---
> This prototype was built for the SIH 2026 hackathon. The system addresses the gap between informal scrap collectors and the formal recycling ecosystem by providing price transparency, traceable handovers, and economic incentives for proper recycling.