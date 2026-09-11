import streamlit as st
import requests
import pandas as pd

# Configure page
st.set_page_config(
    page_title="Kabadiwala Connect - Recycler Dashboard",
    page_icon="♻️",
    layout="wide"
)

# API base URL (adjust for your environment)
API_BASE_URL = "http://localhost:8000"

st.title("♻️ Kabadiwala Connect - Recycler Dashboard")
st.markdown("---")

# Sidebar
st.sidebar.header("Dashboard Navigation")
page = st.sidebar.radio("Select Page", ["Transactions", "Material Categories", "Health Check"])

if page == "Transactions":
    st.header("Recent Transactions")
    
    try:
        response = requests.get(f"{API_BASE_URL}/transactions")
        if response.status_code == 200:
            transactions = response.json()
            
            if transactions:
                df = pd.DataFrame(transactions)
                df.columns = ["Transaction ID", "Lot ID", "Recycler", "Amount (₹)", "Status", "Date"]
                st.dataframe(df, use_container_width=True)
                
                # Summary statistics
                total_amount = df["Amount (₹)"].sum()
                total_transactions = len(df)
                
                col1, col2 = st.columns(2)
                col1.metric("Total Transactions", total_transactions)
                col2.metric("Total Value", f"₹{total_amount:,.2f}")
            else:
                st.info("No transactions found. The system is ready for transactions.")
        else:
            st.error(f"Failed to fetch transactions: {response.status_code}")
    except requests.exceptions.ConnectionError:
        st.error("Could not connect to the backend server. Please ensure the FastAPI server is running.")
    except Exception as e:
        st.error(f"An error occurred: {str(e)}")

elif page == "Material Categories":
    st.header("Material Categories and Rates")
    
    try:
        response = requests.get(f"{API_BASE_URL}/categories")
        if response.status_code == 200:
            categories = response.json()
            
            df = pd.DataFrame(categories)
            df.columns = ["Material ID", "Name", "Category", "Rate per kg (₹)"]
            st.dataframe(df, use_container_width=True)
            
            # Visual representation
            st.subheader("Rate Comparison")
            chart_data = df[["Name", "Rate per kg (₹)"]].set_index("Name")
            st.bar_chart(chart_data)
        else:
            st.error(f"Failed to fetch categories: {response.status_code}")
    except requests.exceptions.ConnectionError:
        st.error("Could not connect to the backend server. Please ensure the FastAPI server is running.")
    except Exception as e:
        st.error(f"An error occurred: {str(e)}")

elif page == "Health Check":
    st.header("System Health")
    
    try:
        response = requests.get(f"{API_BASE_URL}/health")
        if response.status_code == 200:
            health_data = response.json()
            st.success("✅ Backend server is healthy")
            st.json(health_data)
        else:
            st.error(f"Health check failed: {response.status_code}")
    except requests.exceptions.ConnectionError:
        st.error("❌ Could not connect to the backend server. Please ensure the FastAPI server is running.")
    except Exception as e:
        st.error(f"An error occurred: {str(e)}")

# Footer
st.markdown("---")
st.markdown("© 2026 Kabadiwala Connect - Connecting informal collectors with authorized recyclers")
