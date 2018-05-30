package com.example.harsh.ml_kit.util;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class ImagePickerUtil extends Fragment {

    public interface OnImagePickerListener {

        void success(String name, String path);

        void fail(String message);
    }

    public static final String TAG = ImagePickerUtil.class.getSimpleName();
    private static final int CAMERA_PIC_REQUEST = 2000;
    private static final int IMAGE_PICKER_REQUEST = CAMERA_PIC_REQUEST + 1;
    private static final int MEMORY_PERMISSION_REQUEST = IMAGE_PICKER_REQUEST + 1;
    private OnImagePickerListener listener;
    private String mediaPath;


    public static void add(@NonNull FragmentManager manager, @NonNull OnImagePickerListener listener) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(ImagePickerUtil.newInstance(listener), TAG);
        transaction.commit();
    }

    private static ImagePickerUtil newInstance(@NonNull OnImagePickerListener listener) {
        ImagePickerUtil fragment = new ImagePickerUtil();
        fragment.setOnImagePickerListener(listener);
        return fragment;
    }

    public void setOnImagePickerListener(OnImagePickerListener listener) {
        this.listener = listener;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isStoragePermissionGranted()) {
            showGalleryDialog();
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission is granted");
                return true;
            } else {
                Log.d(TAG, "Permission is revoked");
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MEMORY_PERMISSION_REQUEST);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.d(TAG, "Permission is granted");
            return true;
        }
    }

    private void showGalleryDialog() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (items[which].equals("Take Photo")) {
                    dialog.dismiss();
                    Intent pictureIntent = new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE);
                    if (pictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        //Create a file to store the image
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            Log.d("ImagePickerUtil: ", ex.getMessage());
                            // Error occurred while creating the File
                        }
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(getContext(),
                                    "com.example.harsh.ml_kit.provider", photoFile);
                            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    photoURI);
                            startActivityForResult(pictureIntent,
                                    CAMERA_PIC_REQUEST);
                        }
                    }

                } else if (items[which].equals("Choose from Library")) {
                    dialog.dismiss();
                    Intent galleryIntent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, IMAGE_PICKER_REQUEST);
                } else if (items[which].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mediaPath = image.getAbsolutePath();
        return image;
    }

    private String handleCameraResult() {
        //Scale down the image to reduce size.
        mediaPath = BitmapUtils.scaleImage(getContext(), mediaPath, BitmapUtils.DEFAULT_PHOTO_WIDH,
                BitmapUtils.DEFAULT_PHOTO_HEIGHT);
        return mediaPath != null ? "file:///" + mediaPath : null;
    }

    private String handleGalleryResult(Intent intent) {
        String path = BitmapUtils.getImagePath(getContext(), intent);
        if (path.isEmpty()) {
            listener.fail("Please select proper image.");
            return null;
        } else {
            mediaPath = BitmapUtils.scaleImage(getContext(), path, BitmapUtils.DEFAULT_PHOTO_WIDH,
                    BitmapUtils.DEFAULT_PHOTO_HEIGHT);
            return "file:///" + mediaPath;
        }
    }

    //Runtime permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (MEMORY_PERMISSION_REQUEST == requestCode && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            showGalleryDialog();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String filePath = null;
            switch (requestCode) {
                case CAMERA_PIC_REQUEST:
                    filePath = handleCameraResult();
                    break;
                case IMAGE_PICKER_REQUEST:
                    filePath = handleGalleryResult(data);
                    break;
            }
            if (filePath != null) {
                listener.success(filePath.substring(filePath.lastIndexOf("/") + 1), filePath);
            } else {
                listener.fail("Unable to get path");
            }
        }
    }
}