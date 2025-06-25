package com.airbnb.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;

/**
 * WaitUtil provides centralized explicit wait utilities for Selenium WebDriver.
 * <p>
 * Before using wait methods, associate a WebDriver instance with the current thread
 * via {@link #setDriver(WebDriver)}. Wait times and polling intervals are loaded
 * from configuration (test.properties).
 * </p>
 */
public class WaitUtil {

    /** Thread-local storage for WebDriver per test thread. */
    private static final ThreadLocal<WebDriver> driverHolder = new ThreadLocal<>();

    /**
     * Maximum wait time in seconds (from configuration key "wait.timeout.seconds").
     */
    private static final long timeoutSeconds =
            ConfigLoader.getIntProperty("wait.timeout.seconds", 30);

    /**
     * Polling interval in milliseconds between checks (from key "wait.polling.millis").
     */
    private static final long pollingMillis =
            ConfigLoader.getIntProperty("wait.polling.millis", 500);

    private static final Logger log = LoggerFactory.getLogger(WaitUtil.class);
    private static WebDriverWait waitInstance;

    /**
     * Associates a WebDriver with the current thread for subsequent waits.
     * Must be called before any wait utility method.
     *
     * @param driver the WebDriver instance used in the test
     */
    public static void setDriver(WebDriver driver) {
        driverHolder.set(driver);
    }

    /**
     * Retrieves the WebDriver for the current thread or throws if unset.
     *
     * @return the thread-local WebDriver instance
     * @throws IllegalStateException if no WebDriver has been set
     */
    private static WebDriver getDriver() {
        WebDriver driver = driverHolder.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "WebDriver not set. Call WaitUtil.setDriver(driver) first");
        }
        return driver;
    }

    /**
     * Lazily initializes and returns a WebDriverWait configured with timeout and polling.
     *
     * @return a configured WebDriverWait instance
     */
    public static WebDriverWait getWait() {
        if (waitInstance == null) {
            waitInstance = new WebDriverWait(
                    getDriver(), Duration.ofSeconds(timeoutSeconds));
            waitInstance.pollingEvery(Duration.ofMillis(pollingMillis));
        }
        return waitInstance;
    }

    /**
     * Waits until the specified WebElement is clickable (visible and enabled).
     *
     * @param element the WebElement to wait for
     * @return the clickable WebElement once ready
     */
    public static WebElement waitForClickable(WebElement element) {
        return getWait().until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Waits until an element located by the given locator is clickable.
     *
     * @param locator the By locator for the element
     * @return the clickable WebElement once ready
     */
    public static WebElement waitForClickable(By locator) {
        return getWait().until(
                ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Waits until the specified WebElement is visible in the DOM.
     *
     * @param element the WebElement to wait for
     * @return the visible WebElement once ready
     */
    public static WebElement waitForVisible(WebElement element) {
        return getWait().until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Waits until the element located by the given locator is visible.
     *
     * @param locator the By locator for the element
     * @return the visible WebElement once ready
     */
    public static WebElement waitForVisible(By locator) {
        return getWait().until(
                ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits until the page's document.readyState equals "complete".
     * Useful to ensure page load and AJAX calls have finished.
     */
    public static void waitForPageLoad() {
        getWait().until(
                wd -> Objects.equals(
                        ((JavascriptExecutor) wd).executeScript(
                                "return document.readyState"), "complete")
        );
    }

    /**
     * Sleeps the current thread for a specified duration, logging the pause.
     * <p>
     * Use sparingly for non-deterministic waits; prefer explicit waits.
     * </p>
     *
     * @param timeMs the duration to sleep in milliseconds
     */
    public static void sleep(int timeMs) {
        log.info("Sleeping for {} ms", timeMs);
        try {
            Thread.sleep(timeMs);
        } catch (InterruptedException e) {
            log.error("Interrupted during sleep", e);
        }
    }
}