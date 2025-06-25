package com.airbnb.model;

/**
 * Immutable data model representing a summary of an Airbnb listing within search results.
 * <p>
 * Contains key details displayed to users when browsing available accommodations,
 * including pricing, ratings, occupancy breakdown, and booking dates.
 * </p>
 *
 * @param name           the human-readable title of the listing (e.g., "Cozy Downtown Loft")
 * @param rating         average guest rating (scale of 1.0 to 5.0)
 * @param pricePerNight  nightly rate in the local currency
 * @param totalPrice     total cost for the selected booking period (including fees and taxes)
 * @param url            direct link to the listing's detail page
 * @param adults         number of adult guests specified for the search
 * @param children       number of child guests specified for the search
 * @param infants        number of infant guests specified for the search
 * @param pets           number of pets specified for the search
 * @param checkin        check-in date in ISO-8601 format (yyyy-MM-dd)
 * @param checkout       check-out date in ISO-8601 format (yyyy-MM-dd)
 */
public record ListingRecord(
        String name,
        Double rating,
        Double pricePerNight,
        Double totalPrice,
        String url,
        Integer adults,
        Integer children,
        Integer infants,
        Integer pets,
        String checkin,
        String checkout
) {

    /**
     * Creates a new ListingRecord instance with updated URL and booking dates,
     * preserving all other original properties.
     * <p>
     * Useful for tests that first extract or compute the listing details,
     * then navigate to the listing page or change the date range.
     * </p>
     *
     * @param url       new direct link to the listing's page
     * @param checkin   updated check-in date (yyyy-MM-dd)
     * @param checkout  updated check-out date (yyyy-MM-dd)
     * @return a new ListingRecord with the specified URL and dates
     */
    public ListingRecord withUrlAndDates(String url, String checkin, String checkout) {
        // Construct and return a fresh record instance, copying all existing fields except URL and dates
        return new ListingRecord(
                name(),         // original listing title
                rating(),       // original guest rating
                pricePerNight(),// original nightly rate
                totalPrice(),   // original total cost for period
                url,            // override with new URL
                adults(),       // original adult count
                children(),     // original child count
                infants(),      // original infant count
                pets(),         // original pet count
                checkin,        // override with new check-in date
                checkout        // override with new check-out date
        );
    }

    // Note: Records automatically generate equals(), hashCode(), and toString() methods.
    // Use toString() for human-readable logs and assertions in test output.
}