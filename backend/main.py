import sqlite3
import uuid
from datetime import datetime
from typing import List, Optional
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI(title="Kabadiwala Connect API")

# Database setup
DB_PATH = "kabadiwala.db"

def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    conn = get_db()
    cursor = conn.cursor()
    
    # Create tables based on ERD
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS collectors (
            collector_id TEXT PRIMARY KEY,
            name TEXT NOT NULL,
            phone TEXT,
            preferred_language TEXT,
            operating_location TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)
    
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS materials (
            material_id TEXT PRIMARY KEY,
            name TEXT NOT NULL,
            category TEXT NOT NULL
        )
    """)
    
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS recyclers (
            recycler_id TEXT PRIMARY KEY,
            name TEXT NOT NULL,
            location TEXT,
            contact_info TEXT,
            authorized BOOLEAN DEFAULT 1
        )
    """)
    
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS material_lots (
            lot_id TEXT PRIMARY KEY,
            collector_id TEXT,
            source_type TEXT,
            total_weight REAL,
            estimated_value REAL,
            collection_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            status TEXT DEFAULT 'pending',
            FOREIGN KEY (collector_id) REFERENCES collectors(collector_id)
        )
    """)
    
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS lot_materials (
            lot_id TEXT,
            material_id TEXT,
            FOREIGN KEY (lot_id) REFERENCES material_lots(lot_id),
            FOREIGN KEY (material_id) REFERENCES materials(material_id),
            PRIMARY KEY (lot_id, material_id)
        )
    """)
    
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS transactions (
            transaction_id TEXT PRIMARY KEY,
            lot_id TEXT,
            recycler_id TEXT,
            quoted_price REAL,
            final_price REAL,
            transaction_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            status TEXT DEFAULT 'pending',
            FOREIGN KEY (lot_id) REFERENCES material_lots(lot_id),
            FOREIGN KEY (recycler_id) REFERENCES recyclers(recycler_id)
        )
    """)
    
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS payments (
            payment_id TEXT PRIMARY KEY,
            transaction_id TEXT,
            amount REAL,
            payment_method TEXT,
            payment_status TEXT DEFAULT 'pending',
            paid_at TIMESTAMP,
            FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id)
        )
    """)
    
    # Insert realistic sample data for materials with detailed categories
    materials_data = [
        (str(uuid.uuid4()), "Copper Cables", "metals", 250.0),
        (str(uuid.uuid4()), "PCBs", "electronics", 150.0),
        (str(uuid.uuid4()), "CRT Monitors", "electronics", 50.0),
        (str(uuid.uuid4()), "LCD Panels", "electronics", 80.0),
        (str(uuid.uuid4()), "LED Monitors", "electronics", 90.0),
        (str(uuid.uuid4()), "Lithium Batteries", "hazardous", 120.0),
        (str(uuid.uuid4()), "Lead Acid Batteries", "hazardous", 80.0),
        (str(uuid.uuid4()), "Electric Motors", "metals", 120.0),
        (str(uuid.uuid4()), "Aluminum Scrap", "metals", 90.0),
        (str(uuid.uuid4()), "Mixed Plastics", "plastics", 30.0),
        (str(uuid.uuid4()), "Computer Cases", "electronics", 40.0),
        (str(uuid.uuid4()), "Power Supplies", "electronics", 60.0),
        (str(uuid.uuid4()), "Hard Drives", "electronics", 70.0),
        (str(uuid.uuid4()), "Motherboards", "electronics", 100.0),
        (str(uuid.uuid4()), "RAM Modules", "electronics", 45.0),
    ]
    
    for material_id, name, category, rate in materials_data:
        cursor.execute("INSERT OR IGNORE INTO materials (material_id, name, category) VALUES (?, ?, ?)", 
                       (material_id, name, category))
    
    # Insert realistic recyclers with detailed information
    recyclers_data = [
        (str(uuid.uuid4()), "EcoRecyclers Pvt Ltd", "Mumbai", "Maharashtra", "contact@ecorecyclers.com", "+91-22-12345678", "EPR-REC-2022-MH-001", 1, True, "metals,electronics,hazardous", 10),
        (str(uuid.uuid4()), "GreenWaste Solutions", "Pune", "Maharashtra", "info@greenwaste.com", "+91-20-87654321", "EPR-REC-2022-MH-002", 1, True, "electronics,plastics", 15),
        (str(uuid.uuid4()), "MetalMax Recycling", "Nashik", "Maharashtra", "sales@metalmax.com", "+91-253-98765432", "EPR-REC-2022-MH-003", 1, True, "metals", 20),
        (str(uuid.uuid4()), "CleanEarth E-Waste", "Delhi", "Delhi", "delhi@cleanearth.com", "+91-11-23456789", "EPR-REC-2022-DL-001", 1, True, "electronics,hazardous", 25),
        (str(uuid.uuid4()), "RecycleIndia Pvt Ltd", "Bangalore", "Karnataka", "bangalore@recycleindia.com", "+91-80-34567890", "EPR-REC-2022-KA-001", 1, True, "metals,electronics,plastics", 30),
        (str(uuid.uuid4()), "SafeDisposal Systems", "Chennai", "Tamil Nadu", "chennai@safedisposal.com", "+91-44-45678901", "EPR-REC-2022-TN-001", 1, True, "hazardous", 35),
    ]
    
    for recycler_id, name, city, state, email, phone, license, authorized, pickup_available, materials_accepted, service_radius in recyclers_data:
        cursor.execute("INSERT OR IGNORE INTO recyclers (recycler_id, name, location, contact_info, authorized) VALUES (?, ?, ?, ?, ?)", 
                       (recycler_id, name, f"{city}, {state}", f"{email}, {phone}", authorized))
    
    conn.commit()
    conn.close()

# Pydantic models
class MaterialCategory(BaseModel):
    material_id: str
    name: str
    category: str
    estimated_rate_per_kg: float

class CreateLotRequest(BaseModel):
    collector_id: Optional[str] = None
    category: str
    weight_kg: float
    source_type: str = "collected"

class LotResponse(BaseModel):
    lot_id: str
    category: str
    weight_kg: float
    estimated_value: float
    status: str
    created_at: str

class RecyclerOffer(BaseModel):
    recycler_id: str
    name: str
    location: str
    offered_rate: float
    estimated_amount: float

class TransactionRequest(BaseModel):
    lot_id: str
    recycler_name: str
    amount: float
    payment_method: str = "cash"

class TransactionResponse(BaseModel):
    transaction_id: str
    lot_id: str
    recycler_name: str
    amount: float
    payment_method: str
    status: str
    created_at: str

# Initialize database on startup
@app.on_event("startup")
def startup_event():
    init_db()

# Endpoints
@app.get("/health")
def health_check():
    return {"status": "healthy", "timestamp": datetime.now().isoformat()}

@app.get("/categories", response_model=List[MaterialCategory])
def get_categories():
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("SELECT material_id, name, category FROM materials")
    materials = cursor.fetchall()
    
    # Add estimated rates (simplified for prototype)
    rates = {
        "Copper Cables": 250.0,
        "PCBs": 150.0,
        "CRT Monitors": 50.0,
        "LCD Panels": 80.0,
        "Batteries": 100.0,
        "Motors": 120.0,
        "Mixed Plastics": 30.0
    }
    
    result = []
    for material in materials:
        result.append(MaterialCategory(
            material_id=material["material_id"],
            name=material["name"],
            category=material["category"],
            estimated_rate_per_kg=rates.get(material["name"], 50.0)
        ))
    
    conn.close()
    return result

@app.post("/lots", response_model=LotResponse)
def create_lot(request: CreateLotRequest):
    conn = get_db()
    cursor = conn.cursor()
    
    # Get material and rate
    cursor.execute("SELECT material_id, name FROM materials WHERE name = ?", (request.category,))
    material = cursor.fetchone()
    if not material:
        conn.close()
        raise HTTPException(status_code=404, detail="Material category not found")
    
    # Get rate
    rates = {
        "Copper Cables": 250.0,
        "PCBs": 150.0,
        "CRT Monitors": 50.0,
        "LCD Panels": 80.0,
        "LED Monitors": 90.0,
        "Lithium Batteries": 120.0,
        "Lead Acid Batteries": 80.0,
        "Electric Motors": 120.0,
        "Aluminum Scrap": 90.0,
        "Mixed Plastics": 30.0,
        "Computer Cases": 40.0,
        "Power Supplies": 60.0,
        "Hard Drives": 70.0,
        "Motherboards": 100.0,
        "RAM Modules": 45.0,
    }
    rate = rates.get(request.category, 50.0)
    estimated_value = request.weight_kg * rate
    
    # Create collector if not exists
    collector_id = request.collector_id or str(uuid.uuid4())
    cursor.execute("INSERT OR IGNORE INTO collectors (collector_id, name, phone, preferred_language, operating_location) VALUES (?, ?, ?, ?, ?)",
                   (collector_id, "Anonymous Collector", "Unknown", "en", "Unknown"))
    
    # Create lot
    lot_id = str(uuid.uuid4())
    cursor.execute("""
        INSERT INTO material_lots (lot_id, collector_id, source_type, total_weight, estimated_value, status)
        VALUES (?, ?, ?, ?, ?, ?)
    """, (lot_id, collector_id, request.source_type, request.weight_kg, estimated_value, "pending"))
    
    # Link material to lot
    cursor.execute("INSERT INTO lot_materials (lot_id, material_id) VALUES (?, ?)",
                   (lot_id, material["material_id"]))
    
    conn.commit()
    conn.close()
    
    return LotResponse(
        lot_id=lot_id,
        category=request.category,
        weight_kg=request.weight_kg,
        estimated_value=estimated_value,
        status="pending",
        created_at=datetime.now().isoformat()
    )

@app.get("/recyclers/match", response_model=List[RecyclerOffer])
def match_recyclers(lot_id: str):
    conn = get_db()
    cursor = conn.cursor()
    
    # Get lot details
    cursor.execute("SELECT lot_id, estimated_value FROM material_lots WHERE lot_id = ?", (lot_id,))
    lot = cursor.fetchone()
    if not lot:
        conn.close()
        raise HTTPException(status_code=404, detail="Lot not found")
    
    # Get all authorized recyclers
    cursor.execute("SELECT recycler_id, name, location FROM recyclers WHERE authorized = 1")
    recyclers = cursor.fetchall()
    
    result = []
    for recycler in recyclers:
        # More realistic matching with varied rates based on recycler
        # Some recyclers offer better rates, some worse
        import random
        base_rate = lot["estimated_value"]
        variation = random.uniform(0.85, 1.15)  # ±15% variation
        offered_amount = base_rate * variation
        
        result.append(RecyclerOffer(
            recycler_id=recycler["recycler_id"],
            name=recycler["name"],
            location=recycler["location"],
            offered_rate=offered_amount / lot["estimated_value"],
            estimated_amount=offered_amount
        ))
    
    # Sort by offered amount (highest first)
    result.sort(key=lambda x: x.estimated_amount, reverse=True)
    
    conn.close()
    return result

@app.post("/transactions", response_model=TransactionResponse)
def create_transaction(request: TransactionRequest):
    conn = get_db()
    cursor = conn.cursor()
    
    # Get lot
    cursor.execute("SELECT lot_id, estimated_value FROM material_lots WHERE lot_id = ?", (request.lot_id,))
    lot = cursor.fetchone()
    if not lot:
        conn.close()
        raise HTTPException(status_code=404, detail="Lot not found")
    
    # Get recycler
    cursor.execute("SELECT recycler_id FROM recyclers WHERE name = ?", (request.recycler_name,))
    recycler = cursor.fetchone()
    if not recycler:
        conn.close()
        raise HTTPException(status_code=404, detail="Recycler not found")
    
    # Create transaction
    transaction_id = str(uuid.uuid4())
    cursor.execute("""
        INSERT INTO transactions (transaction_id, lot_id, recycler_id, quoted_price, final_price, status)
        VALUES (?, ?, ?, ?, ?, ?)
    """, (transaction_id, request.lot_id, recycler["recycler_id"], request.amount, request.amount, "confirmed"))
    
    # Update lot status
    cursor.execute("UPDATE material_lots SET status = 'confirmed' WHERE lot_id = ?", (request.lot_id,))
    
    # Create payment record
    payment_id = str(uuid.uuid4())
    cursor.execute("""
        INSERT INTO payments (payment_id, transaction_id, amount, payment_method, payment_status, paid_at)
        VALUES (?, ?, ?, ?, ?, ?)
    """, (payment_id, transaction_id, request.amount, request.payment_method, "paid", datetime.now().isoformat()))
    
    conn.commit()
    conn.close()
    
    return TransactionResponse(
        transaction_id=transaction_id,
        lot_id=request.lot_id,
        recycler_name=request.recycler_name,
        amount=request.amount,
        payment_method=request.payment_method,
        status="confirmed",
        created_at=datetime.now().isoformat()
    )

@app.get("/transactions")
def list_transactions():
    conn = get_db()
    cursor = conn.cursor()
    
    cursor.execute("""
        SELECT t.transaction_id, t.lot_id, r.name as recycler_name, t.final_price, 
               t.status, t.transaction_datetime
        FROM transactions t
        JOIN recyclers r ON t.recycler_id = r.recycler_id
        ORDER BY t.transaction_datetime DESC
    """)
    
    transactions = cursor.fetchall()
    result = []
    for tx in transactions:
        result.append({
            "transaction_id": tx["transaction_id"],
            "lot_id": tx["lot_id"],
            "recycler_name": tx["recycler_name"],
            "amount": tx["final_price"],
            "status": tx["status"],
            "created_at": tx["transaction_datetime"]
        })
    
    conn.close()
    return result

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
