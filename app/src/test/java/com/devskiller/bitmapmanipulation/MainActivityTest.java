package com.devskiller.bitmapmanipulation;

import android.os.Build;
import android.widget.Toast;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.TimeUnit;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.robolectric.Robolectric.setupActivity;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = { Build.VERSION_CODES.O })
@PowerMockIgnore({ "android.*", "androidx.*", "org.robolectric.*" })
@PrepareForTest({ Toast.class, Toast.Callback.class })
public class MainActivityTest extends BaseTest {

    @Test
    public void testIdle() throws Exception {
        mockStatic(Toast.class);

        final MainActivity activity = setupActivity(MainActivity.class);

        getBackgroundExecutorService(activity).awaitTermination(EXECUTOR_TIMEOUT_S, TimeUnit.SECONDS);
        Robolectric.flushForegroundThreadScheduler();
        verifyNoMoreInteractions(Toast.class);
    }
}
