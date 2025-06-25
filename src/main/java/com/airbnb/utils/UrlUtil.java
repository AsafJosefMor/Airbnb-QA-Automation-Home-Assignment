package com.airbnb.utils;

import com.airbnb.model.ListingRecord;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for manipulating and parsing Airbnb reservation URLs.
 * <p>
 * Provides methods to replace date query parameters in reservation URLs,
 * and to parse URL parameters into a ListingRecord model for easy validation.
 * </p>
 */
public class UrlUtil {

    /**
     * Replaces the "check_in" and "check_out" query parameters in the given URL.
     * <p>
     * Uses regex lookbehind to target only the parameter values without altering other parts.
     * </p>
     *
     * @param url             the original reservation URL containing date parameters
     * @param newCheckInIso   the new ISO-formatted check-in date (yyyy-MM-dd)
     * @param newCheckOutIso  the new ISO-formatted check-out date (yyyy-MM-dd)
     * @return a new URL string with updated check-in and check-out values
     */
    public static String replaceDatesInUrl(String url, String newCheckInIso, String newCheckOutIso) {
        // Replace only the value after "check_in=" up to the next '&'
        url = url.replaceAll("(?<=check_in=)[^&]+", newCheckInIso);
        // Replace only the value after "check_out=" up to the next '&'
        url = url.replaceAll("(?<=check_out=)[^&]+", newCheckOutIso);
        return url;
    }

    /**
     * Parses query parameters from a reservation URL into a ListingRecord.
     * <p>
     * Extracts numbers of adults, children, infants, pets, and the check-in/checkout dates.
     * All other ListingRecord fields (name, rating, price) are left null.
     * </p>
     *
     * @param fullUrl the reservation URL including query parameters
     * @return a ListingRecord populated with guest counts and date values
     * @throws IllegalArgumentException if the URL is malformed or parameters are invalid
     */
    public static ListingRecord parseUrl(String fullUrl) {
        try {
            URL url = new URL(fullUrl);

            // Split query string into key/value pairs
            String query = url.getQuery();
            Map<String, String> params = new HashMap<>();
            if (query != null) {
                for (String kv : query.split("&")) {
                    String[] pair = kv.split("=", 2);
                    if (pair.length == 2) {
                        // Decode keys/values to handle URL encoding
                        String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                        params.put(key, value);
                    }
                }
            }

            // Parse numeric guest counts, defaulting to 0 if absent
            int adults   = Integer.parseInt(params.getOrDefault("numberOfAdults",   "0"));
            int children = Integer.parseInt(params.getOrDefault("numberOfChildren", "0"));
            int infants  = Integer.parseInt(params.getOrDefault("numberOfInfants",  "0"));
            int pets     = Integer.parseInt(params.getOrDefault("numberOfPets",     "0"));
            // Extract ISO date strings for check-in/checkout
            String checkin  = params.get("checkin");
            String checkout = params.get("checkout");

            // Construct ListingRecord; listing details fields are null here
            return new ListingRecord(
                    null, null, null, null,
                    fullUrl,
                    adults, children, infants, pets,
                    checkin, checkout
            );
        } catch (Exception e) {
            // Wrap any URL or parsing exceptions
            throw new IllegalArgumentException("Invalid reservation URL: " + fullUrl, e);
        }
    }
}