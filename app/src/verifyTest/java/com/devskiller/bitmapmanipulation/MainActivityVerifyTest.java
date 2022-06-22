package com.devskiller.bitmapmanipulation;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.exifinterface.media.ExifInterface;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.field;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.robolectric.Robolectric.setupActivity;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = { Build.VERSION_CODES.O })
@PowerMockIgnore({ "android.*", "androidx.*", "org.mockito.*", "org.robolectric.*" })
@PrepareForTest({ Bitmap.class, BitmapFactory.class, ExifInterface.class, ImageProcessor.class, Toast.class, Toast.Callback.class })
public class MainActivityVerifyTest extends BaseTest {

    @Test
    public void testProperProcessing() throws Exception {
        mockStatic(Bitmap.class);
        mockStatic(BitmapFactory.class);
        mockStatic(Toast.class);
        final Toast toastMock = mock(Toast.class);
        final ExifInterface exifInterfaceMock = mock(ExifInterface.class);
        final Bitmap bitmapFromReadingOptionsMock = mock(Bitmap.class);
        final Bitmap bitmapFromDecodingMock = mock(Bitmap.class);
        final Bitmap bitmapFromEncodingMock = mock(Bitmap.class);
        final ArgumentCaptor<OutputStream> outputStreamCaptor = ArgumentCaptor.forClass(OutputStream.class);

        final int maxDimension = RuntimeEnvironment.application.getResources().getInteger(R.integer.image_max_dimension);
        final MainActivity activity = setupActivity(MainActivity.class);

        when(BitmapFactory.decodeStream(any(InputStream.class), isNull(Rect.class), any(BitmapFactory.Options.class)))
                .thenReturn(bitmapFromReadingOptionsMock)
                .thenReturn(bitmapFromDecodingMock);
        when(bitmapFromDecodingMock.getWidth()).thenReturn(maxDimension);
        when(bitmapFromDecodingMock.getHeight()).thenReturn(maxDimension);
        doNothing().when(bitmapFromDecodingMock).getPixels(
                any(int[].class), eq(0), eq(maxDimension), eq(0), eq(0), eq(maxDimension), eq(maxDimension));
        when(Bitmap.createBitmap(any(int[].class), eq(maxDimension), eq(maxDimension), eq(Bitmap.Config.ARGB_8888)))
                .thenReturn(bitmapFromEncodingMock);
        when(bitmapFromEncodingMock.compress(
                eq(Bitmap.CompressFormat.JPEG), eq(100), outputStreamCaptor.capture())).thenAnswer(new Answer<Boolean>() {

                    @Override
                    public Boolean answer(InvocationOnMock invocation) throws Exception {
                        final String fileOutputStreamPath =
                                (String) field(FileOutputStream.class, "path").get(outputStreamCaptor.getValue());
                        final String mainActivityOutputDirectoryPath =
                                (String) field(MainActivity.class, "OUTPUT_DIRECTORY_PATH").get(activity);
                        assertTrue(fileOutputStreamPath.contains(mainActivityOutputDirectoryPath));
                        return true;
                    }
                });
        whenNew(ExifInterface.class).withAnyArguments().thenReturn(exifInterfaceMock);
        doNothing().when(exifInterfaceMock).setGpsInfo(any(Location.class));
        doNothing().when(exifInterfaceMock).setAttribute(eq(ExifInterface.TAG_IMAGE_DESCRIPTION), anyString());
        doNothing().when(exifInterfaceMock).saveAttributes();
        when(Toast.makeText(eq(activity), eq(R.string.share_image_success), eq(Toast.LENGTH_SHORT))).thenReturn(toastMock);
        doNothing().when(toastMock).show();

        method(MainActivity.class, "openGallery").invoke(activity);
        ShadowActivity shadowActivity = shadowOf(activity);
        ShadowActivity.IntentForResult galleryIntent = shadowActivity.getNextStartedActivityForResult();
        shadowActivity.receiveResult(galleryIntent.intent, Activity.RESULT_OK,
                new Intent().setData(Uri.parse(activity.getClass().getClassLoader().getResource("test0.jpg").toString())));
        getBackgroundExecutorService(activity).awaitTermination(EXECUTOR_TIMEOUT_S, TimeUnit.SECONDS);
        Robolectric.flushForegroundThreadScheduler();
        verifyStatic(Toast.class, times(1));
        Toast.makeText(activity, R.string.share_image_success, Toast.LENGTH_SHORT);
        verify(toastMock).show();
        verifyNoMoreInteractions(Toast.class);
    }

    @Test
    public void testImageScaledDown() throws Exception {
        mockStatic(Bitmap.class);
        mockStatic(BitmapFactory.class);
        mockStatic(Toast.class);
        final Toast toastMock = mock(Toast.class);
        final ExifInterface exifInterfaceMock = mock(ExifInterface.class);
        final Bitmap bitmapFromReadingOptionsMock = mock(Bitmap.class);
        final Bitmap bitmapFromDecodingMock = mock(Bitmap.class);
        final Bitmap bitmapFromEncodingMock = mock(Bitmap.class);
        final ArgumentCaptor<BitmapFactory.Options> readOptionsCaptor = ArgumentCaptor.forClass(BitmapFactory.Options.class);
        final ArgumentCaptor<OutputStream> outputStreamCaptor = ArgumentCaptor.forClass(OutputStream.class);

        final int maxDimension = RuntimeEnvironment.application.getResources().getInteger(R.integer.image_max_dimension);
        final MainActivity activity = setupActivity(MainActivity.class);

        when(BitmapFactory.decodeStream(any(InputStream.class), isNull(Rect.class), readOptionsCaptor.capture()))
                .thenAnswer(new Answer<Bitmap>() {

                    @Override
                    public Bitmap answer(InvocationOnMock invocation) {
                        final BitmapFactory.Options options = readOptionsCaptor.getValue();
                        options.outWidth = (maxDimension * 2);
                        options.outHeight = (maxDimension * 2);
                        return bitmapFromReadingOptionsMock;
                    }
                }).thenReturn(bitmapFromDecodingMock);
        when(bitmapFromDecodingMock.getWidth()).thenReturn(maxDimension);
        when(bitmapFromDecodingMock.getHeight()).thenReturn(maxDimension);
        doNothing().when(bitmapFromDecodingMock).getPixels(
                any(int[].class), eq(0), eq(maxDimension), eq(0), eq(0), eq(maxDimension), eq(maxDimension));
        when(Bitmap.createBitmap(any(int[].class), eq(maxDimension), eq(maxDimension), eq(Bitmap.Config.ARGB_8888)))
                .thenReturn(bitmapFromEncodingMock);
        when(bitmapFromEncodingMock.compress(
                eq(Bitmap.CompressFormat.JPEG), eq(100), outputStreamCaptor.capture())).thenReturn(true);
        whenNew(ExifInterface.class).withAnyArguments().thenReturn(exifInterfaceMock);
        when(Toast.makeText(eq(activity), eq(R.string.share_image_success), eq(Toast.LENGTH_SHORT))).thenReturn(toastMock);
        doNothing().when(toastMock).show();

        method(MainActivity.class, "openGallery").invoke(activity);
        ShadowActivity shadowActivity = shadowOf(activity);
        ShadowActivity.IntentForResult galleryIntent = shadowActivity.getNextStartedActivityForResult();
        shadowActivity.receiveResult(galleryIntent.intent, Activity.RESULT_OK,
                new Intent().setData(Uri.parse(activity.getClass().getClassLoader().getResource("test0.jpg").toString())));
        getBackgroundExecutorService(activity).awaitTermination(EXECUTOR_TIMEOUT_S, TimeUnit.SECONDS);
        Robolectric.flushForegroundThreadScheduler();
        final List<BitmapFactory.Options> readOptionsCollection = readOptionsCaptor.getAllValues();
        assertEquals(2, readOptionsCollection.size());
        assertTrue(readOptionsCollection.get(0).inJustDecodeBounds);
        assertEquals(2, readOptionsCollection.get(1).inSampleSize);
    }

    @Test
    public void testImagePixelsInverted() throws Exception {
        mockStatic(Bitmap.class);
        mockStatic(BitmapFactory.class);
        mockStatic(Toast.class);
        final Toast toastMock = mock(Toast.class);
        final ExifInterface exifInterfaceMock = mock(ExifInterface.class);
        final Bitmap bitmapFromReadingOptionsMock = mock(Bitmap.class);
        final Bitmap bitmapFromDecodingMock = mock(Bitmap.class);
        final Bitmap bitmapFromEncodingMock = mock(Bitmap.class);
        final ArgumentCaptor<int[]> intArrayCaptor = ArgumentCaptor.forClass(int[].class);

        final int maxDimension = RuntimeEnvironment.application.getResources().getInteger(R.integer.image_max_dimension);
        final MainActivity activity = setupActivity(MainActivity.class);

        when(BitmapFactory.decodeStream(any(InputStream.class), isNull(Rect.class), any(BitmapFactory.Options.class)))
                .thenReturn(bitmapFromReadingOptionsMock)
                .thenReturn(bitmapFromDecodingMock);
        when(bitmapFromDecodingMock.getWidth()).thenReturn(maxDimension);
        when(bitmapFromDecodingMock.getHeight()).thenReturn(maxDimension);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) {
                assertEquals(1, intArrayCaptor.getAllValues().size());
                final int[] intArray = intArrayCaptor.getAllValues().get(0);
                for (int i = 0; i < intArray.length; ++i) {
                    intArray[i] = (0xFFFFFF - i);
                }
                return null;
            }
        }).when(bitmapFromDecodingMock).getPixels(
                intArrayCaptor.capture(), eq(0), eq(maxDimension), eq(0), eq(0), eq(maxDimension), eq(maxDimension));
        when(Bitmap.createBitmap(intArrayCaptor.capture(), eq(maxDimension), eq(maxDimension), eq(Bitmap.Config.ARGB_8888)))
                .thenAnswer(new Answer<Bitmap>() {

                    @Override
                    public Bitmap answer(InvocationOnMock invocation) {
                        assertEquals(2, intArrayCaptor.getAllValues().size());
                        assertEquals(intArrayCaptor.getAllValues().get(0).length, intArrayCaptor.getAllValues().get(1).length);
                        final int[] intArray = intArrayCaptor.getAllValues().get(1);
                        for (int i = 0; i < intArray.length; ++i) {
                            assertEquals(0xFF000000, (intArray[i] & 0xFF000000));
                            assertEquals(i, (intArray[i] & 0xFFFFFF));
                        }

                        return bitmapFromEncodingMock;
                    }
                });
        when(bitmapFromEncodingMock.compress(eq(Bitmap.CompressFormat.JPEG), eq(100), any(OutputStream.class))).thenReturn(true);
        whenNew(ExifInterface.class).withAnyArguments().thenReturn(exifInterfaceMock);
        when(Toast.makeText(eq(activity), eq(R.string.share_image_success), eq(Toast.LENGTH_SHORT))).thenReturn(toastMock);
        doNothing().when(toastMock).show();

        method(MainActivity.class, "openGallery").invoke(activity);
        ShadowActivity shadowActivity = shadowOf(activity);
        ShadowActivity.IntentForResult galleryIntent = shadowActivity.getNextStartedActivityForResult();
        shadowActivity.receiveResult(galleryIntent.intent, Activity.RESULT_OK,
                new Intent().setData(Uri.parse(activity.getClass().getClassLoader().getResource("test0.jpg").toString())));
        getBackgroundExecutorService(activity).awaitTermination(EXECUTOR_TIMEOUT_S, TimeUnit.SECONDS);
        Robolectric.flushForegroundThreadScheduler();
    }

    @Test
    public void testExifDataProvided() throws Exception {
        mockStatic(Bitmap.class);
        mockStatic(BitmapFactory.class);
        mockStatic(Toast.class);
        final Toast toastMock = mock(Toast.class);
        final ExifInterface exifInterfaceMock = mock(ExifInterface.class);
        final Bitmap bitmapFromReadingOptionsMock = mock(Bitmap.class);
        final Bitmap bitmapFromDecodingMock = mock(Bitmap.class);
        final Bitmap bitmapFromEncodingMock = mock(Bitmap.class);

        final int maxDimension = RuntimeEnvironment.application.getResources().getInteger(R.integer.image_max_dimension);
        final MainActivity activity = setupActivity(MainActivity.class);

        when(BitmapFactory.decodeStream(any(InputStream.class), isNull(Rect.class), any(BitmapFactory.Options.class)))
                .thenReturn(bitmapFromReadingOptionsMock)
                .thenReturn(bitmapFromDecodingMock);
        when(bitmapFromDecodingMock.getWidth()).thenReturn(maxDimension);
        when(bitmapFromDecodingMock.getHeight()).thenReturn(maxDimension);
        doNothing().when(bitmapFromDecodingMock).getPixels(
                any(int[].class), eq(0), eq(maxDimension), eq(0), eq(0), eq(maxDimension), eq(maxDimension));
        when(Bitmap.createBitmap(any(int[].class), eq(maxDimension), eq(maxDimension), eq(Bitmap.Config.ARGB_8888)))
                .thenReturn(bitmapFromEncodingMock);
        when(bitmapFromEncodingMock.compress(eq(Bitmap.CompressFormat.JPEG), eq(100), any(OutputStream.class))).thenReturn(true);
        whenNew(ExifInterface.class).withAnyArguments().thenReturn(exifInterfaceMock);
        doNothing().when(exifInterfaceMock).setGpsInfo(any(Location.class));
        doNothing().when(exifInterfaceMock).setAttribute(eq(ExifInterface.TAG_IMAGE_DESCRIPTION), anyString());
        doNothing().when(exifInterfaceMock).saveAttributes();
        when(Toast.makeText(eq(activity), eq(R.string.share_image_success), eq(Toast.LENGTH_SHORT))).thenReturn(toastMock);
        doNothing().when(toastMock).show();

        method(MainActivity.class, "openGallery").invoke(activity);
        ShadowActivity shadowActivity = shadowOf(activity);
        ShadowActivity.IntentForResult galleryIntent = shadowActivity.getNextStartedActivityForResult();
        shadowActivity.receiveResult(galleryIntent.intent, Activity.RESULT_OK,
                new Intent().setData(Uri.parse(activity.getClass().getClassLoader().getResource("test0.jpg").toString())));
        getBackgroundExecutorService(activity).awaitTermination(EXECUTOR_TIMEOUT_S, TimeUnit.SECONDS);
        Robolectric.flushForegroundThreadScheduler();
        verify(exifInterfaceMock).setGpsInfo(any(Location.class));
        verify(exifInterfaceMock).setAttribute(eq(ExifInterface.TAG_IMAGE_DESCRIPTION), anyString());
        verify(exifInterfaceMock).saveAttributes();
        verifyNoMoreInteractions(exifInterfaceMock);
    }

    @Test
    public void testOutputImageSaved() throws Exception {
        mockStatic(Bitmap.class);
        mockStatic(BitmapFactory.class);
        mockStatic(Toast.class);
        final Toast toastMock = mock(Toast.class);
        final ExifInterface exifInterfaceMock = mock(ExifInterface.class);
        final Bitmap bitmapFromReadingOptionsMock = mock(Bitmap.class);
        final Bitmap bitmapFromDecodingMock = mock(Bitmap.class);
        final Bitmap bitmapFromEncodingMock = mock(Bitmap.class);

        final int maxDimension = RuntimeEnvironment.application.getResources().getInteger(R.integer.image_max_dimension);
        final MainActivity activity = setupActivity(MainActivity.class);

        when(BitmapFactory.decodeStream(any(InputStream.class), isNull(Rect.class), any(BitmapFactory.Options.class)))
                .thenReturn(bitmapFromReadingOptionsMock)
                .thenReturn(bitmapFromDecodingMock);
        when(bitmapFromDecodingMock.getWidth()).thenReturn(maxDimension);
        when(bitmapFromDecodingMock.getHeight()).thenReturn(maxDimension);
        doNothing().when(bitmapFromDecodingMock).getPixels(
                any(int[].class), eq(0), eq(maxDimension), eq(0), eq(0), eq(maxDimension), eq(maxDimension));
        when(Bitmap.createBitmap(any(int[].class), eq(maxDimension), eq(maxDimension), eq(Bitmap.Config.ARGB_8888)))
                .thenReturn(bitmapFromEncodingMock);
        when(bitmapFromEncodingMock.compress(eq(Bitmap.CompressFormat.JPEG), eq(100), any(OutputStream.class))).thenReturn(true);
        whenNew(ExifInterface.class).withAnyArguments().thenReturn(exifInterfaceMock);
        when(Toast.makeText(eq(activity), eq(R.string.share_image_success), eq(Toast.LENGTH_SHORT))).thenReturn(toastMock);
        doNothing().when(toastMock).show();

        method(MainActivity.class, "openGallery").invoke(activity);
        ShadowActivity shadowActivity = shadowOf(activity);
        ShadowActivity.IntentForResult galleryIntent = shadowActivity.getNextStartedActivityForResult();
        shadowActivity.receiveResult(galleryIntent.intent, Activity.RESULT_OK,
                new Intent().setData(Uri.parse(activity.getClass().getClassLoader().getResource("test0.jpg").toString())));
        getBackgroundExecutorService(activity).awaitTermination(EXECUTOR_TIMEOUT_S, TimeUnit.SECONDS);
        Robolectric.flushForegroundThreadScheduler();
        final File outputDirectory = new File((String) field(MainActivity.class, "OUTPUT_DIRECTORY_PATH").get(null));
        assertTrue(outputDirectory.isDirectory());
        final File[] outputFiles = outputDirectory.listFiles();
        assertEquals(1, outputFiles.length);
        assertTrue(outputFiles[0].getName().endsWith(".jpeg") || outputFiles[0].getName().endsWith(".jpg"));
    }
}
