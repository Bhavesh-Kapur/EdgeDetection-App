package com.example.edgedetectionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.edgedetectionapp.databinding.ActivityMainBinding;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;


public class MainActivity extends AppCompatActivity {
    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewRequestBuilder;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private ImageView outputImage;

    private GLSurfaceView glView;
    private CameraGLRenderer glRenderer;
    private TextView fpsText;
    private int frameCount = 0;
    private long lastTime = 0;
    private boolean isEdgeDetectionOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(textureListener);
//        outputImage = findViewById(R.id.outputImage);
//        GLSurfaceView glView = findViewById(R.id.glSurfaceView);
//        CameraGLRenderer glRenderer = new CameraGLRenderer();
//
//        glView.setEGLContextClientVersion(2);
//        glView.setRenderer(glRenderer);
//        glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        glView = findViewById(R.id.glSurfaceView);
        glRenderer = new CameraGLRenderer();

        glView.setEGLContextClientVersion(2);
        glView.setRenderer(glRenderer);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        fpsText = findViewById(R.id.fpsText);
        lastTime = System.currentTimeMillis();

//        Button toggleButton = findViewById(R.id.toggleEdgeButton);
//        toggleButton.setOnClickListener(v -> {
//            isEdgeDetectionOn = !isEdgeDetectionOn;
//            if (isEdgeDetectionOn) {
//                toggleButton.setText("Turn Off Edge Detection");
//                // start edge detection processing
//            } else {
//                toggleButton.setText("Turn On Edge Detection");
//                // stop edge detection, show raw camera
//            }
//        });
//
//        // Initialize the button text:
//        toggleButton.setText("Turn Off Edge Detection");
//    }

        Button toggleButton = findViewById(R.id.toggleEdgeButton);
        toggleButton.setOnClickListener(v -> {
            isEdgeDetectionOn = !isEdgeDetectionOn;
            toggleButton.setText(isEdgeDetectionOn ? "Turn Off Edge Detection" : "Turn On Edge Detection");
        });
    }
    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}
        @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) { return false; }
        @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            Bitmap bitmap = textureView.getBitmap();
            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap, mat);
            if(isEdgeDetectionOn){Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY);
                processFrameNative(mat.getNativeObjAddr());}

            Bitmap resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, resultBitmap);
            runOnUiThread(() -> {
                glRenderer.updateBitmap(resultBitmap);
                glView.queueEvent(() -> glView.requestRender());
            });

            frameCount++;
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTime >= 1000) {
                final int fps = frameCount;
                frameCount = 0;
                lastTime = currentTime;

                runOnUiThread(() -> fpsText.setText("FPS: " + fps));
            }
        }
    };

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0]; // Back camera
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
                return;
            }

            manager.openCamera(cameraId, stateCallback, backgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(640, 480);
            Surface surface = new Surface(texture);

            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) return;
                    captureSession = session;
                    previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                    try {
                        captureSession.setRepeatingRequest(previewRequestBuilder.build(), null, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

//    @Override protected void onResume() {
//        super.onResume();
//        startBackgroundThread();
//        if (textureView.isAvailable()) {
//            openCamera();
//        } else {
//            textureView.setSurfaceTextureListener(textureListener);
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Unable to load OpenCV");
        } else {
            Log.d("OpenCV", "OpenCV loaded");
        }

        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static {
        System.loadLibrary("native-lib");
    }

    public native void processFrameNative(long matAddr);
}
