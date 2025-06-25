package com.airbnb.enums;

/**
 * Enumeration of API endpoint paths used within the Airbnb QA automation project.
 * <p>
 * Each constant represents a specific endpoint fragment that can be appended to
 * the base URL to form a complete request URL.
 * </p>
 * <p>
 * This design centralizes endpoint definitions to avoid hardcoding strings
 * throughout the test code, making maintenance and updates easier.
 * </p>
 */
public enum Endpoints {

    /**
     * Endpoint path for making a reservation on Airbnb.
     * <p>
     * This path corresponds to URLs of the form:
     * <code>https://www.airbnb.com/book/stays/{listingId}?{queryParams}</code>
     * </p>
     */
    RESERVATION("/book/stays");

    /**
     * The relative path fragment for this endpoint.
     * <p>
     * Stored as a String so that it can be concatenated with the base URL
     * for HTTP requests in tests.
     * </p>
     */
    private final String path;

    /**
     * Constructor for the enum constant.
     *
     * @param path the URL fragment for this endpoint (must start with a slash).
     */
    Endpoints(String path) {
        this.path = path;
    }

    /**
     * Retrieves the URL fragment associated with this endpoint.
     *
     * @return the endpoint path (e.g., "/book/stays").
     */
    public String getPath() {
        return path;
    }
}