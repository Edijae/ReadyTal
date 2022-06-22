package com.devskiller.bitmapmanipulation;

import android.content.ContentResolver;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;

import com.arasthel.asyncjob.AsyncJob;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;

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

                //START CHANGES
                //END CHANGES
            }
        }, mBackgroundExecutorService);
    }

    private int calculateInSampleSize(
        final @NonNull BitmapFactory.Options options,
        int maxDimension
    ) {
        //START CHANGES
        return 0;
        //END CHANGES
    }

    private void addExtraData(
        final @NonNull String bitmapPath,
        final @NonNull String note,
        final @NonNull Location location
    ) throws IOException {
        //START CHANGES
        //END CHANGES
    }

    private void callCallbackOnMainThread(@NonNull AsyncJob.OnMainThreadJob job) {
        AsyncJob.doOnMainThread(job);
    }
}
