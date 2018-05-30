package com.example.harsh.ml_kit.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;

import java.util.List;

public class ImageLabelingActivity extends AppCompatActivity {

    private TextView textView;
    private ImageView imageView;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_labeling);

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
                        startImageLabeling(bitmap);
                    }

                    @Override
                    public void fail(String message) {
                        Toast.makeText(ImageLabelingActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });


    }

    private void startImageLabeling(Bitmap bitmap) {
        FirebaseVisionLabelDetectorOptions options =
                new FirebaseVisionLabelDetectorOptions.Builder()
                        .setConfidenceThreshold(0.8f)
                        .build();


        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionLabelDetector detector = FirebaseVision.getInstance()
                .getVisionLabelDetector(options);

        Task<List<FirebaseVisionLabel>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionLabel>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionLabel> labels) {
                                        // Task completed successfully
                                        StringBuilder result = new StringBuilder();
                                        int count = 0;
                                        for (FirebaseVisionLabel label : labels) {
                                            String text = label.getLabel();
                                            String entityId = label.getEntityId();
                                            float confidence = label.getConfidence();
                                            ++count;
                                            result.append("\n" + count + ": Text: ").append(text)
                                                    .append(", \n entityId: ").append(entityId)
                                                    .append(",\n confidence: ").append(confidence);

                                        }
                                        if (!result.toString().isEmpty())
                                            textView.setText(result);
                                        else
                                            textView.setText("No Results Found!!!");

                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        textView.setText(e.getMessage());
                                    }
                                });
    }


}
