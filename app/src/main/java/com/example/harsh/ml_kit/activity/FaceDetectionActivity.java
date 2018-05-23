package com.example.harsh.ml_kit.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.example.harsh.ml_kit.CameraSource;
import com.example.harsh.ml_kit.CameraSourcePreview;
import com.example.harsh.ml_kit.facedetection.FaceDetectionProcessor;
import com.example.harsh.ml_kit.R;
import com.example.harsh.ml_kit.util.GraphicOverlay;

import java.io.IOException;

public class FaceDetectionActivity extends AppCompatActivity {

    private static final String TAG = "FaceDetectionActivity";

    private CameraSource cameraSource = null;

    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);

        ToggleButton toggleCameraRotate = findViewById(R.id.toggle_camera_rotate);
        toggleCameraRotate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "Set facing");
                if (cameraSource != null) {
                    if (isChecked) {
                        cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
                    } else {
                        cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
                    }
                }
                preview.stop();
                startCameraSource();
            }
        });

        preview = findViewById(R.id.firePreview);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = findViewById(R.id.fireFaceOverlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }
        createCameraSource();
        startCameraSource();


    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }

        Log.i(TAG, "Using Face Detector Processor");
        cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor());


    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }
}
