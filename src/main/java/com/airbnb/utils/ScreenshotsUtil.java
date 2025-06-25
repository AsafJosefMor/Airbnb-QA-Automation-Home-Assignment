package com.airbnb.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * Utility class for capturing browser screenshots using Chrome DevTools Protocol (CDP).
 * <p>
 * Provides methods to take screenshots of the browser viewport and save them
 * with a timestamped filename for failed UI test cases.
 * </p>
 */
public class ScreenshotsUtil {

    private static final String OUTPUT_DIR = "test-output/screenshots";
    private static final String FILE_PREFIX = "failed-";
    private static final String FILE_EXTENSION = ".jpg";

    /**
     * Captures a screenshot via ChromeDriver's CDP interface and writes it to disk.
     * <p>
     * The screenshot is encoded in Base64 by CDP, decoded, and saved as a JPEG file
     * in the <code>test-output/screenshots</code> directory, with the current timestamp
     * in "yyyy-MM-dd-HH-mm-ss" format.
     * </p>
     *
     * @param driver the WebDriver instance, must be a ChromeDriver
     * @throws IOException if writing the image file fails
     * @throws ClassCastException if the driver is not an instance of ChromeDriver
     */
    public static void takeScreenshotChrome(WebDriver driver) throws IOException {
        // Get current timestamp for unique filename
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String timestamp = dateFormat.format(date);

        // Prepare output directory
        Path outputPath = Path.of(OUTPUT_DIR);
        // Ensure directory exists; create if necessary
        Files.createDirectories(outputPath);

        // Use Chrome DevTools Protocol (CDP) to capture screenshot
        // The command returns a Map containing a Base64-encoded "data" key
        Map<String, Object> result = ((ChromeDriver) driver).executeCdpCommand(
                "Page.captureScreenshot",
                Map.of(
                        "fromSurface", true
                        // "captureBeyondViewport": true can be enabled for full-page captures
                )
        );
        String base64Data = result.get("data").toString();

        // Decode the Base64-encoded image data to a byte array
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

        // Construct the full file path: e.g., test-output/screenshots/failed-2025-06-24-12-34-56.jpg
        Path screenshotFile = outputPath.resolve(FILE_PREFIX + timestamp + FILE_EXTENSION);

        // Write the decoded image bytes to the output file
        Files.write(screenshotFile, imageBytes);
    }
}