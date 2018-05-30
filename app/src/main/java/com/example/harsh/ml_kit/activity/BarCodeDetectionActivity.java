package com.example.harsh.ml_kit.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.harsh.ml_kit.R;
import com.example.harsh.ml_kit.util.BitmapUtils;
import com.example.harsh.ml_kit.util.ImagePickerUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.util.List;

public class BarCodeDetectionActivity extends AppCompatActivity {

    private TextView textView;
    private ImageView imageView;
    private String filePath;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_code_detection);

        textView = findViewById(R.id.text_result);
        imageView = findViewById(R.id.image_view);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePickerUtil.add(getSupportFragmentManager(), new ImagePickerUtil.OnImagePickerListener() {
                    @Override
                    public void success(String name, String path) {
                        filePath = path.replace("file:///", "");
                        Bitmap bitmap;
                        bitmap = BitmapUtils.decodeSampledBitmapFromFile(filePath, 500, 600);
                        imageView.setImageBitmap(bitmap);
                        startBarCodeLabeling(bitmap);
                    }

                    @Override
                    public void fail(String message) {
                        Toast.makeText(BarCodeDetectionActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });


    }

    private void startBarCodeLabeling(Bitmap bitmap) {

        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_QR_CODE,
                                FirebaseVisionBarcode.FORMAT_AZTEC,
                                FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
                        .build();


//        Image image=null;
//        FirebaseVisionImage image = FirebaseVisionImage.fromMediaImage(image, rotation);

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);


        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options);

        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        getInfoFromBarCodes(barcodes);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        Toast.makeText(BarCodeDetectionActivity.this, e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation(String cameraId, Activity activity, Context context)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // On most devices, the sensor orientation is 90 degrees, but for some
        // devices it is 270 degrees. For devices with a sensor orientation of
        // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
        CameraManager cameraManager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);
        rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;

        // Return the corresponding FirebaseVisionImageMetadata rotation value.
        int result;
        switch (rotationCompensation) {
            case 0:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                break;
            case 90:
                result = FirebaseVisionImageMetadata.ROTATION_90;
                break;
            case 180:
                result = FirebaseVisionImageMetadata.ROTATION_180;
                break;
            case 270:
                result = FirebaseVisionImageMetadata.ROTATION_270;
                break;
            default:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                Log.e("BarCode Activity", "Bad rotation value: " + rotationCompensation);
        }
        return result;
    }

    private void getInfoFromBarCodes(List<FirebaseVisionBarcode> barcodes) {
        StringBuilder result = new StringBuilder();
        int count = 0;
        for (FirebaseVisionBarcode barcode : barcodes) {
            Rect bounds = barcode.getBoundingBox();
            Point[] corners = barcode.getCornerPoints();
            count++;
            if (bounds != null)
                result.append("\n" + count + ". Bounds: " + bounds.left + "," + bounds.right +
                        "," + bounds.top + "," + bounds.bottom);
            if (corners != null)
                result.append("\n Corners: " + corners.toString());

            int valueType = barcode.getValueType();
            // See API reference for complete list of supported types
            switch (valueType) {
                case FirebaseVisionBarcode.TYPE_WIFI:
                    String ssid = barcode.getWifi().getSsid();
                    String password = barcode.getWifi().getPassword();
                    int type = barcode.getWifi().getEncryptionType();
                    result.append("\n SSID:" + ssid + ", Password: " + password + ", TYPE: " + type);
                    break;
                case FirebaseVisionBarcode.TYPE_URL:
                    String title = barcode.getUrl().getTitle();
                    String url = barcode.getUrl().getUrl();
                    result.append("\n TITLE: " + title + ", URL: " + url);
                    break;
            }
        }
        if (barcodes.size() == 0)
            textView.setText("No Results Found");
        else
            textView.setText(result);
    }
}
