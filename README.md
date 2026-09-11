# Kabadiwala Connect - Crude Working Prototype

A vernacular, low-literacy, offline-tolerant mobile platform that enables informal scrap collectors to discover fair prices, connect directly with authorized recyclers, complete documented and traceable material handovers, and receive payment.

## 🚀 Quick Start

### Backend (FastAPI + SQLite)
```bash
cd backend
python -m venv venv
# On Windows: venv\Scripts\activate
# On Mac/Linux: source venv/bin/activate
pip install -r requirements.txt
python -m uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

The backend will be available at `http://localhost:8000` with interactive docs at `http://localhost:8000/docs`

### Dashboard (Streamlit)
```bash
cd dashboard
pip install -r requirements.txt
streamlit run app.py --server.headless true
```

The dashboard will be available at `http://localhost:8501`

### Android App
The Android project structure is set up with Room DB for offline storage. To build and run:
1. Open the `android` directory in Android Studio
2. Sync Gradle dependencies
3. Run on emulator or device

## 📋 Architecture Overview

### Backend API (FastAPI)
- **Database**: SQLite (easily swappable to MySQL)
- **Endpoints**:
  - `GET /health` - Health check
  - `GET /categories` - Get material categories with rates
  - `POST /lots` - Create material lot with price estimate
  - `GET /recyclers/match?lot_id={id}` - Match recyclers for a lot
  - `POST /transactions` - Confirm transaction
  - `GET /transactions` - List all transactions

### Android App (Native Java)
- **Offline-first**: Room DB (SQLite) for local storage
- **Background Sync**: WorkManager for syncing when online
- **Entities**: Collector, Material, Recycler, MaterialLot, Transaction
- **UI**: Basic material lot creation with price estimation

### Dashboard (Streamlit)
- **Features**: Transaction listing, material categories with rates, health check
- **Real-time**: Connects to FastAPI backend for live data

## 🧪 Testing the API

```bash
# Health check
curl http://localhost:8000/health

# Get categories
curl http://localhost:8000/categories

# Create a lot
curl -X POST http://localhost:8000/lots \
  -H "Content-Type: application/json" \
  -d '{"category": "Copper Cables", "weight_kg": 2.5}'

# Match recyclers (use lot_id from previous response)
curl "http://localhost:8000/recyclers/match?lot_id={lot_id}"

# Confirm transaction
curl -X POST http://localhost:8000/transactions \
  -H "Content-Type: application/json" \
  -d '{"lot_id": "{lot_id}", "recycler_name": "EcoRecyclers Pvt Ltd", "amount": 625.0, "payment_method": "cash"}'

# List transactions
curl http://localhost:8000/transactions
```

## 🗄️ Database Schema

The database implements the ERD with the following tables:
- **collectors**: Collector profiles with language preferences
- **materials**: Material categories and types
- **recyclers**: Authorized recyclers with contact info
- **material_lots**: Digital lots of collected materials
- **lot_materials**: Many-to-many relationship between lots and materials
- **transactions**: Transaction records with pricing
- **payments**: Payment tracking and status

## 🌐 Offline-First Architecture

- **Android**: Room DB stores data locally, WorkManager syncs when online
- **Backend**: Stateless API accepts data whenever the app connects
- **Dashboard**: Real-time view of synchronized data

## 📱 Android Features Implemented

- ✅ Room Database entities matching backend schema
- ✅ Basic UI for material lot creation
- ✅ Price estimation based on material category and weight
- ✅ Offline storage capability
- ⏳ API integration (needs Retrofit implementation)
- ⏳ WorkManager background sync
- ⏳ Text-to-Speech for low-literacy support
- ⏳ Multi-language support (Hindi/Marathi)

## 🔧 Next Steps for Full Implementation

1. **Android App Enhancement**:
   - Complete Retrofit API integration
   - Implement WorkManager for background sync
   - Add Text-to-Speech functionality
   - Implement multi-language support (strings.xml)
   - Add camera integration for material photos
   - Implement TensorFlow Lite for image classification

2. **Backend Enhancement**:
   - Add authentication/authorization
   - Implement price trend prediction with Scikit-learn
   - Add GPS location tracking
   - Implement advanced recycler matching algorithm
   - Add payment integration (UPI DeepLink)

3. **Dashboard Enhancement**:
   - Add detailed transaction analytics
   - Implement recycler management interface
   - Add price trend visualization
   - Include earnings tracking for collectors

4. **Testing & Deployment**:
   - Field testing with real collectors
   - Performance optimization
   - Security audit
   - Production deployment setup

## 📊 End-to-End Flow

1. **Collector** selects material category and enters weight
2. **App** calculates estimated value using local rates
3. **App** creates digital lot in local Room DB
4. **App** syncs lot to backend when online
5. **Backend** matches lot with authorized recyclers
6. **Collector** selects recycler and confirms handover
7. **Backend** records transaction and payment
8. **Dashboard** shows real-time transaction data
9. **App** updates local DB with transaction status

## 🛠️ Technology Stack

- **Backend**: FastAPI, SQLite, Python 3.13
- **Android**: Native Java, Room DB, WorkManager, Retrofit
- **Dashboard**: Streamlit, Pandas, Requests
- **AI/ML**: TensorFlow Lite (Android), Scikit-learn (Backend - planned)

## 📝 Notes

- This is a crude prototype focused on core functionality
- Database uses SQLite for simplicity; can be migrated to MySQL
- Android app needs additional development for full feature set
- Security, authentication, and error handling need enhancement
- Field testing required to validate user experience

## 🤝 Contributing

This prototype was built for the SIH 2026 hackathon. The system addresses the gap between informal scrap collectors and the formal recycling ecosystem by providing price transparency, traceable handovers, and economic incentives for proper recycling.
