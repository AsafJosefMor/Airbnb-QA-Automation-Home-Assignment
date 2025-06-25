package com.airbnb.tests;

import com.airbnb.pages.BasePage;
import com.airbnb.utils.ConfigLoader;
import com.airbnb.utils.ScreenshotsUtil;
import com.airbnb.utils.WaitUtil;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;

import java.io.IOException;

/**
 * BaseTest provides setup and teardown for all UI automation tests.
 * <p>
 * Responsibilities include:
 * <ul>
 *   <li>Initializing WebDriver with Chrome options.</li>
 *   <li>Loading test configuration (URL, dates, guest counts).</li>
 *   <li>Handling screenshot capture on test failures.</li>
 *   <li>Quitting the browser session after the suite.</li>
 * </ul>
 * </p>
 */
public class BaseTest {

    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    /** The WebDriver instance controlling the browser. */
    protected WebDriver driver;
    /** Base URL for Airbnb, loaded from configuration. */
    protected String url;
    /** Default search criteria loaded from configuration. */
    protected String location, checkin, checkout;
    protected int adults, children, infants, pets;
    /** The BasePage representing the common Airbnb search menu. */
    protected BasePage basePage;

    /**
     * One-time setup before any tests run: configures ChromeDriver and sets up WaitUtil.
     */
    @BeforeSuite
    public void setUp() {
        log.info("Setting up WebDriver and test suite");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu=true");
        options.addArguments("--blink-settings=imagesEnabled=false");
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
        WaitUtil.setDriver(driver);
        log.debug("WebDriver initialized with headless Chrome");
    }

    /**
     * Load test-specific configuration before each test class and navigate to the base URL.
     */
    @BeforeClass
    protected void loadConfig() {
        log.info("Loading test configuration");
        url      = ConfigLoader.appUrl();
        location = ConfigLoader.location();
        checkin  = ConfigLoader.checkin();
        checkout = ConfigLoader.checkout();
        adults   = ConfigLoader.adults();
        children = ConfigLoader.children();
        infants  = ConfigLoader.infants();
        pets     = ConfigLoader.pets();
        log.debug("Configuration: url={} location={} dates={}-{} guests={},{},{},{}",
                url, location, checkin, checkout, adults, children, infants, pets);
        driver.get(url);
        basePage = new BasePage(driver);
        log.info("Navigated to base URL");
    }

    /**
     * Teardown after each test method: capture a screenshot if the test failed.
     *
     * @param result the ITestResult containing test status
     * @throws IOException if screenshot writing fails
     */
    @AfterMethod
    public void tearDownMethod(ITestResult result) throws IOException {
        if (result.getStatus() == ITestResult.FAILURE) {
            log.warn("Test {} failed: capturing screenshot", result.getName());
            ScreenshotsUtil.takeScreenshotChrome(driver);
        }
    }

    /**
     * One-time teardown after all tests complete: quit the browser session.
     */
    @AfterSuite
    public void tearDownSuite() {
        log.info("Tearing down WebDriver");
        if (driver != null) {
            driver.quit();
            log.debug("WebDriver session ended");
        }
    }
}