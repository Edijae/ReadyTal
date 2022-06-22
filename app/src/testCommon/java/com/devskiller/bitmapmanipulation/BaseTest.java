package com.devskiller.bitmapmanipulation;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.powermock.modules.junit4.rule.PowerMockRule;

import java.io.File;
import java.util.concurrent.ExecutorService;

import androidx.annotation.NonNull;

import static org.powermock.api.mockito.PowerMockito.field;

abstract class BaseTest {

    static final long EXECUTOR_TIMEOUT_S = 1L;

    @Rule
    public PowerMockRule powerMockRule = new PowerMockRule();

    @Before
    public void setUp() throws Exception {
        cleanUpOutputDirectory();
    }

    @After
    public void tearDown() throws Exception {
        cleanUpOutputDirectory();
    }

    @NonNull
    ExecutorService getBackgroundExecutorService(@NonNull MainActivity activity) throws Exception {
        final ImageProcessor imageProcessor = (ImageProcessor) field(MainActivity.class, "mImageProcessor").get(activity);
        return (ExecutorService) field(ImageProcessor.class, "mBackgroundExecutorService").get(imageProcessor);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void cleanUpOutputDirectory() throws Exception {
        File outputDirectory = new File((String) field(MainActivity.class, "OUTPUT_DIRECTORY_PATH").get(null));
        FileUtils.deleteDirectory(outputDirectory);
        outputDirectory.mkdirs();
    }
}
