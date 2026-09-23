package com.kabadiwala.connect.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecureStorage {
    private static final String TAG = "SecureStorage";
    private static final String PREFS_NAME = "secure_prefs";
    private static final String KEY_ALIAS = "kabadiwala_key";
    
    private SharedPreferences prefs;
    private Context context;
    
    public SecureStorage(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Store data securely (simple XOR encryption for prototype)
     * In production, use Android KeyStore with proper encryption
     */
    public void storeSecure(String key, String value) {
        try {
            String encrypted = encrypt(value);
            prefs.edit().putString(key, encrypted).apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to store secure data", e);
            // Fallback to plain storage (not ideal for production)
            prefs.edit().putString(key, value).apply();
        }
    }
    
    /**
     * Retrieve secure data
     */
    public String getSecure(String key, String defaultValue) {
        try {
            String encrypted = prefs.getString(key, null);
            if (encrypted == null) return defaultValue;
            return decrypt(encrypted);
        } catch (Exception e) {
            Log.e(TAG, "Failed to retrieve secure data", e);
            return prefs.getString(key, defaultValue);
        }
    }
    
    /**
     * Simple encryption (XOR with device ID as key)
     * In production, use proper AES encryption with Android KeyStore
     */
    private String encrypt(String data) {
        String deviceId = SecurityUtils.getDeviceId(context);
        StringBuilder encrypted = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            encrypted.append((char) (data.charAt(i) ^ deviceId.charAt(i % deviceId.length())));
        }
        return Base64.encodeToString(encrypted.toString().getBytes(), Base64.DEFAULT);
    }
    
    /**
     * Simple decryption
     */
    private String decrypt(String encryptedData) {
        String deviceId = SecurityUtils.getDeviceId(context);
        byte[] decoded = Base64.decode(encryptedData, Base64.DEFAULT);
        StringBuilder decrypted = new StringBuilder();
        for (int i = 0; i < decoded.length; i++) {
            decrypted.append((char) (decoded[i] ^ deviceId.charAt(i % deviceId.length())));
        }
        return decrypted.toString();
    }
    
    /**
     * Clear all stored data
     */
    public void clearAll() {
        prefs.edit().clear().apply();
    }
    
    /**
     * Check if data exists
     */
    public boolean contains(String key) {
        return prefs.contains(key);
    }
    
    /**
     * Remove specific key
     */
    public void remove(String key) {
        prefs.edit().remove(key).apply();
    }
}
