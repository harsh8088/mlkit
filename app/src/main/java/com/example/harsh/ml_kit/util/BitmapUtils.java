package com.example.harsh.ml_kit.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BitmapUtils {

    private static final String TEMP_IMG_DIRECTORY = "MLKIT";
    private static final String IMG_EXTENSION = ".jpg";
    public static final int DEFAULT_PHOTO_WIDH = 540;
    public static final int DEFAULT_PHOTO_HEIGHT = 960;

    private BitmapUtils() {
    }

    /**
     * Find out the path of the saved image with the help of uri
     *
     * @param context Application context
     * @param data    Intent with the image Uri used to query for path.
     * @return Absolute path of the image else null.
     */
    public static String getImagePath(Context context, Intent data) {
        Uri selectedImage = data.getData();
        if (selectedImage == null)
            return null;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String path = cursor.getString(columnIndex);
        cursor.close();
        return path;
    }

    /**
     * Find out the path of the saved video with the help of uri.
     *
     * @param context Application context
     * @param data    Intent with Video Uri used to query for path.
     * @return Absolute path of the video else null.
     */
    public static String getVideoPath(Context context, Intent data) {
        Uri uri = data.getData();
        if (uri == null)
            return null;
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return null;
    }

    /**
     * Scale the image based on the passed parameters.
     *
     * @param context Application context to show Toast.
     * @param path    Absolute path of the image.
     * @param width   Output image width.
     * @param height  Output image height.
     * @return Scaled image else null.
     */
    public static String scaleImage(Context context, String path, int width, int height) {
        try {
            Bitmap bitmap = decodeSampledBitmapFromFile(
                    path, width, height);
            if (bitmap == null) {
                Toast.makeText(context, "Image not supported.",
                        Toast.LENGTH_LONG).show();
            } else {
                Matrix matrix = new Matrix();
                ExifInterface exif = new ExifInterface(path);
                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
                int rotate = 0;
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotate -= 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotate -= 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotate -= -90;
                        break;
                }
                matrix.setRotate(rotate);
                Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                return saveBitmap(scaledBitmap);
            }
        } catch (Exception e) {
            Log.d("BitmapUtil:scaleImage ",e.getMessage());
        }
        return null;
    }

    /**
     * Get path used for saving the scaled images to file system.
     *
     * @return
     */
    public static String scaledImagePath() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File file = new File(tempDirectory(), "img_" + timeStamp + IMG_EXTENSION);
        return file.getAbsolutePath();
    }

    /**
     * Get directory to saved the media.
     *
     * @return
     */
    public static String tempDirectory() {
        File directory = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                TEMP_IMG_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory.getAbsolutePath();
    }

    /**
     * Generate the thumbnail of provided image.
     *
     * @param path   Absolute path of the image
     * @param width  Thumbnail width
     * @param height Thumbnail height
     * @return Thumbnail image saved path.
     */
    public static String generateThumbnail(String path, int width, int height) {
        try {
            Matrix matrix = new Matrix();
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            int rotate = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate -= 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate -= 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate -= -90;
                    break;
            }
            matrix.setRotate(rotate);
            File file = new File(path);
            Bitmap bitmap = decodeSampledBitmapFromFile(
                    file.getAbsolutePath(), width, height);
            Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            String thumbPath = tempDirectory() + "/thumb" + IMG_EXTENSION;
            OutputStream stream = new FileOutputStream(thumbPath);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            stream.close();
            return thumbPath;
        } catch (Exception e) {
            Log.d("BitmapUtil:genThumb ",e.getMessage());

        }
        return null;
    }

    /**
     * Save the bitmap to file system.
     *
     * @param bmp Bitmap to be saved.
     * @return Path of the saved Bitmap.
     */
    public static String saveBitmap(Bitmap bmp) {
        try {
            String scaledImagePath = scaledImagePath();
            OutputStream stream = new FileOutputStream(scaledImagePath);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            stream.close();
            return scaledImagePath;
        } catch (Exception e) {
            Log.d("BitmapUtil:saveBitmap",e.getMessage());

        }
        return null;
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth,
                                                     int reqHeight) { // BEST QUALITY MATCH
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        // Calculate inSampleSize, Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
//        options.inPreferredConfig = Bitmap.Config.RGB_565;
        //ARGB_8888 Config is used for FirebaseVision
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        int inSampleSize = 1;
        if (height > reqHeight) {
            inSampleSize = Math.round((float) height / (float) reqHeight);
        }
        int expectedWidth = width / inSampleSize;
        if (expectedWidth > reqWidth) {
            inSampleSize = Math.round((float) width / (float) reqWidth);
        }
        options.inSampleSize = inSampleSize;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * Merge two images
     *
     * @param bottomImage Bottom image for merge
     * @param topImage    Top image for merge
     * @return Merged image
     */
    public static Bitmap mergeImages(Bitmap bottomImage, Bitmap topImage) {
        final Bitmap output = Bitmap.createBitmap(bottomImage.getWidth(),
                bottomImage.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawBitmap(bottomImage, 0, 0, paint);
        canvas.drawBitmap(topImage,
                (bottomImage.getWidth() - topImage.getWidth()) / 2,
                (bottomImage.getHeight() - topImage.getHeight()) / 2, paint);
        return output;
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream is;
        Bitmap bitmap = null;
        try {
            is = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}