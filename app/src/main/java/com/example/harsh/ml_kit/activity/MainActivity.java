package com.example.harsh.ml_kit.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.harsh.ml_kit.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";


    private static final int PERMISSION_REQUESTS = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }
    }


    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(MainActivity.this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    public void onButtonClick(View view) {

        Intent intent = null;

        switch (view.getId()) {
            case R.id.btn_text_detection:
                intent = new Intent(MainActivity.this, TextDetectionActivity.class);
                break;
            case R.id.btn_barcode_detection:
//                intent = new Intent(MainActivity.this, BarCodeDetectionActivity.class);
                Toast.makeText(MainActivity.this, "coming soon!!!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_image_labeling:
//                intent = new Intent(MainActivity.this, ImageLabelingActivity.class);
                Toast.makeText(MainActivity.this, "coming soon!!!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_face_detection:
                intent = new Intent(MainActivity.this, FaceDetectionActivity.class);
                break;
            case R.id.btn_landmark_detection:
//                intent = new Intent(MainActivity.this, LandMarkDetectionActivity.class);
                Toast.makeText(MainActivity.this, "coming soon!!!", Toast.LENGTH_SHORT).show();
                break;
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
