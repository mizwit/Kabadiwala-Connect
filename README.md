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
- **Authentication**: Mobile number + 4-digit PIN system with device binding
- **Home Page**: Central hub with 4 main sections (Safety, Enter Lot, History, Earnings)
- **Offline-first**: Room DB (SQLite) for local storage
- **Security**: SHA-256 PIN hashing with device ID salt, encrypted storage
- **Network**: Multi-URL fallback (LAN, emulator, localhost)
- **UI**: Fixed header layout, emoji-based home page buttons, consistent styling
- **Features**: Registration flow, PIN login, lot creation, recycler matching, transaction confirmation, earnings tracking
- **Entities**: Collector, Material, Recycler, MaterialLot, Transaction

### Dashboard (Streamlit)
- **Features**: Transaction listing, material categories with rates, health check
- **Real-time**: Connects to FastAPI backend for live data

## Authentication Flow

1. **Registration**:
   - User enters name and mobile number
   - Backend generates 4-digit OTP (shown in response for prototype)
   - User verifies OTP
   - User creates and confirms 4-digit PIN
   - Backend stores PIN hash with device binding
   - Backend generates auth token (stored locally encrypted)

2. **Login**:
   - User enters mobile number and 4-digit PIN
   - App verifies PIN against backend (offline-capable with local hash)
   - Backend refreshes auth token and device binding
   - All API calls include Bearer token authorization
   - Supports re-authentication after app reinstall

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
  -d '{"phone":"9876543210","name":"Test User","device_id":"test_device","pin_hash":"hashed_pin"}'

# Login
curl -X POST http://localhost:8000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"9876543210","pin_hash":"hashed_pin","device_id":"test_device"}'

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
- **collectors**: Collector profiles with authentication data (includes pin_hash for device binding)
- **materials**: Material categories and types
- **recyclers**: Authorized recyclers with contact info
- **material_lots**: Digital lots of collected materials
- **lot_materials**: Many-to-many relationship between lots and materials
- **transactions**: Transaction records with pricing
- **payments**: Payment tracking and status
- **otp_storage**: Temporary OTP storage for authentication
- **auth_tokens**: Long-lived authentication tokens with device association

## Technology Stack

- **Backend**: FastAPI, SQLite, Python 3.13
- **Android**: Native Java, Room DB, Retrofit, OkHttp, Gradle 8.5
- **Dashboard**: Streamlit, Pandas, Requests
- **Security**: SHA-256 hashing, device ID salt, encrypted storage
- **AI/ML**: TensorFlow Lite (planned for image classification)

## End-to-End Flow

1. **Collector** registers with name, mobile number, and 4-digit PIN
2. **Collector** logs in using mobile number and PIN
3. **App** displays home page with 4 main sections:
   - Safety Information
   - Enter Lot
   - Transaction History
   - Total Earnings
4. **Collector** selects "Enter Lot" and chooses material category
5. **Collector** enters weight and app calculates estimated value
6. **App** creates digital lot in local Room DB
7. **App** syncs lot to backend when online (with auth token)
8. **Backend** matches lot with authorized recyclers
9. **Collector** selects recycler and confirms handover
10. **Backend** records transaction and payment
11. **Dashboard** shows real-time transaction data
12. **App** updates local DB with transaction status
13. **Collector** can view transaction history and earnings from home page

## Development Notes

- **Gradle Version**: 8.5 (compatible with Java 21)
- **Android SDK**: 34
- **Min SDK**: 21 (Android 5.0+)
- **Target SDK**: 34 (Android 14)
- **Java Version**: 17 (compile/target)
- **Network Configuration**: 
  - Backend runs on `0.0.0.0:8000` for LAN access
  - Android app uses LAN IP (currently `192.168.1.3:8000`) for physical devices
  - Emulator uses `10.0.2.2:8000` for localhost access
  - Windows Firewall must allow port 8000 for device connections

---
> This prototype was built for the SIH 2026 hackathon. The system addresses the gap between informal scrap collectors and the formal recycling ecosystem by providing price transparency, traceable handovers, and economic incentives for proper recycling.