package com.airbnb.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Centralized loader for test configuration properties.
 * <p>
 * Loads default values from a <code>test.properties</code> file on the classpath,
 * then allows overriding via system properties or environment variables.
 * Lookup priority for any key:
 * <ol>
 *   <li>Java system property (e.g., <code>-Dkey=value</code>)</li>
 *   <li>Environment variable (e.g., <code>export key=value</code>)</li>
 *   <li>Property from <code>test.properties</code> file</li>
 * </ol>
 * </p>
 */
public class ConfigLoader {

    /**
     * Holds loaded default properties from test.properties file.
     */
    private static final Properties PROPERTIES = new Properties();

    // Static initializer loads defaults at class-load time
    static {
        // Attempt to load the properties file from classpath
        try (InputStream is = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("test-properties/test.properties")) {
            if (is != null) {
                PROPERTIES.load(is);
            } else {
                System.err.println("Warning: test.properties not found on classpath");
            }
        } catch (IOException e) {
            // Fail fast if file present but unreadable
            throw new ExceptionInInitializerError(
                    "Failed to load test.properties: " + e.getMessage());
        }
    }

    /**
     * Retrieves the property value for a given key.
     * <p>
     * Checks system properties first, then environment variables, then defaults.
     * </p>
     *
     * @param key the configuration key to look up
     * @return the configured value or <code>null</code> if not set in any source
     */
    public static String getProperty(String key) {
        // 1) Check Java system properties
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.trim().isEmpty()) {
            return systemValue;
        }

        // 2) Check environment variables
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue;
        }

        // 3) Fallback to loaded defaults from test.properties
        return PROPERTIES.getProperty(key);
    }

    /**
     * Retrieves an integer property with a default fallback.
     * <p>
     * Parses the string value or returns <code>defaultValue</code> if unset or invalid.
     * </p>
     *
     * @param key the configuration key
     * @param defaultValue fallback if value is missing or non-integer
     * @return parsed integer or <code>defaultValue</code>
     */
    public static int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("Invalid integer for key " + key + ": " + value);
            return defaultValue;
        }
    }

    /**
     * Retrieves a boolean property, parsing "true"/"false" (case-insensitive).
     *
     * @param key the configuration key
     * @param defaultValue fallback if value is missing
     * @return parsed boolean or <code>defaultValue</code>
     */
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    // Convenience accessors for frequently used keys:

    /** @return the application base URL */
    public static String appUrl() {
        return getProperty("app.url");
    }

    /** @return default search location (e.g., "Tel Aviv") */
    public static String location() {
        return getProperty("search.location");
    }

    /** @return default check-in date in MM/dd/yyyy format */
    public static String checkin() {
        return getProperty("search.checkin");
    }

    /** @return default check-out date in MM/dd/yyyy format */
    public static String checkout() {
        return getProperty("search.checkout");
    }

    /** @return number of adults for search defaults */
    public static int adults() {
        return getIntProperty("search.adults", 0);
    }

    /** @return number of children for search defaults */
    public static int children() {
        return getIntProperty("search.children", 0);
    }

    /** @return number of infants for search defaults */
    public static int infants() {
        return getIntProperty("search.infants", 0);
    }

    /** @return number of pets for search defaults */
    public static int pets() {
        return getIntProperty("search.pets", 0);
    }
}