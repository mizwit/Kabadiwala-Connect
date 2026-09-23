package com.kabadiwala.connect.security;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SecurityUtils {
    private static final String TAG = "SecurityUtils";
    
    /**
     * Get unique device ID for salting
     */
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
    
    /**
     * Hash a PIN with device ID as salt using SHA-256
     */
    public static String hashPin(Context context, String pin) {
        try {
            String deviceId = getDeviceId(context);
            String saltedPin = deviceId + pin;
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(saltedPin.getBytes());
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Failed to hash PIN", e);
            return null;
        }
    }
    
    /**
     * Verify PIN against stored hash
     */
    public static boolean verifyPin(Context context, String inputPin, String storedHash) {
        String inputHash = hashPin(context, inputPin);
        return inputHash != null && inputHash.equals(storedHash);
    }
    
    /**
     * Generate a random 4-digit PIN for initial setup
     */
    public static String generateRandomPin() {
        SecureRandom random = new SecureRandom();
        return String.format("%04d", random.nextInt(10000));
    }
    
    /**
     * Validate PIN format (4 digits)
     */
    public static boolean isValidPin(String pin) {
        return pin != null && pin.matches("\\d{4}");
    }
}
