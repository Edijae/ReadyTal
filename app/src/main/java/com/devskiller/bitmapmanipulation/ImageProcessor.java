package com.devskiller.bitmapmanipulation;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

import com.arasthel.asyncjob.AsyncJob;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class ImageProcessor {

    private static final int COMPRESS_QUALITY_HINT = 100;

    interface ProcessImageCallback {

        void onSuccess(@NonNull String outputPath);

        void onFailure();
    }

    private final ExecutorService mBackgroundExecutorService = Executors.newSingleThreadExecutor();
    @SuppressWarnings("FieldCanBeLocal")
    private final int mMaxDimension;
    @SuppressWarnings("FieldCanBeLocal")
    private final ContentResolver mContentResolver;

    ImageProcessor(int maxDimension, @NonNull ContentResolver contentResolver) {
        mMaxDimension = maxDimension;
        mContentResolver = contentResolver;
    }

    @SuppressWarnings("ConstantConditions")
    void processImage(
        final @NonNull Uri streamSource,
        final @NonNull String outputDirectoryPath,
        final @NonNull String note,
        final @NonNull Location location,
        final @NonNull ProcessImageCallback callback
    ) {
        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {

            @SuppressWarnings("ResultOfMethodCallIgnored")
            @Override
            public void doOnBackground() {
                final String fileName = "bitmapmanipulator_" + System.currentTimeMillis() + ".jpeg";
                new File(outputDirectoryPath).mkdirs();
                final String outputPath = (outputDirectoryPath + fileName);
                boolean success = true;

                //START CHANGES
                try{
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    InputStream input = mContentResolver.openInputStream(streamSource);
                    BitmapFactory.decodeStream(input, null, options);

                    // Calculate inSampleSize
                    int sampleSize = calculateInSampleSize(options,mMaxDimension);

                    // Decode bitmap with inSampleSize set
                    BitmapFactory.Options options2 = new BitmapFactory.Options();
                    options2.inJustDecodeBounds = false;
                    options2.inSampleSize = sampleSize;

                    InputStream input2 = mContentResolver.openInputStream(streamSource);
                    Bitmap bitmap = BitmapFactory.decodeStream(input2, null, options2);
                    Bitmap inverted = invert(bitmap);
                    bitmap.recycle();

                    File out = new File(outputPath);
                    FileOutputStream outputStream = new FileOutputStream(out);
                    inverted.compress(Bitmap.CompressFormat.JPEG,COMPRESS_QUALITY_HINT,outputStream);
                    outputStream.close();
                    inverted.recycle();
                }catch (Exception exception){
                    exception.printStackTrace();
                    success = false;
                }finally {
                    final boolean finalSuccess = success;
                    callCallbackOnMainThread(new AsyncJob.OnMainThreadJob() {
                        @Override
                        public void doInUIThread() {
                            if(finalSuccess){
                                callback.onSuccess(outputPath);
                            }else
                                callback.onFailure();
                        }
                    });
                    if(success){
                        try {
                            addExtraData(outputPath,note, location);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //END CHANGES
            }
        }, mBackgroundExecutorService);
    }

    public Bitmap invert(Bitmap src)
    {
        int height = src.getHeight();
        int width = src.getWidth();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        Paint paint = new Paint();

        ColorMatrix matrixGrayscale = new ColorMatrix();
        matrixGrayscale.setSaturation(0);

        ColorMatrix matrixInvert = new ColorMatrix();
        matrixInvert.set(new float[]
                {
                        -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
                        0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
                        0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                });
        matrixInvert.preConcat(matrixGrayscale);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrixInvert);
        paint.setColorFilter(filter);

        canvas.drawBitmap(src, 0, 0, paint);
        return bitmap;
    }

    private int calculateInSampleSize(
        final @NonNull BitmapFactory.Options options,
        int maxDimension
    ) {
        //START CHANGES
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > maxDimension || width > maxDimension) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= maxDimension
                    && (halfWidth / inSampleSize) >= maxDimension) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
        //END CHANGES
    }

    private void addExtraData(
        final @NonNull String bitmapPath,
        final @NonNull String note,
        final @NonNull Location location
    ) throws IOException {
        //START CHANGES
        ExifInterface exif = new ExifInterface(bitmapPath);
        exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION,note);
        exif.setGpsInfo(location);
        exif.saveAttributes();
        //END CHANGES
    }

    private void callCallbackOnMainThread(@NonNull AsyncJob.OnMainThreadJob job) {
        AsyncJob.doOnMainThread(job);
    }
}
