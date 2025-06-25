package com.airbnb.tests;

import com.airbnb.enums.GuestType;
import com.airbnb.pages.ReservationPage;
import com.airbnb.pages.SearchResultsPage;
import com.airbnb.pages.ListingPage;
import com.airbnb.model.ListingRecord;
import com.airbnb.utils.TimeUtil;
import com.airbnb.utils.UrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * End-to-end test workflow for searching and reserving an Airbnb listing.
 * <p>
 * Test steps:
 * <ol>
 *   <li>Search for stays with specified criteria.</li>
 *   <li>Select the top listing sorted by rating and price.</li>
 *   <li>Confirm listing page details (dates and guest counts).</li>
 *   <li>Adjust guest counts and verify update.</li>
 *   <li>Change booking dates via URL and verify.</li>
 *   <li>Complete reservation and validate URL parameters.</li>
 * </ol>
 * </p>
 */
public class SearchEditReserveTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(SearchEditReserveTest.class);

    /** The highest-ranked and cheapest listing selected for reservation. */
    private ListingRecord topListing;
    /** Page object representing an individual listing detail page. */
    private ListingPage listingPage;
    /** Page object for search results and pagination. */
    private SearchResultsPage results;
    /** All listings collected from search results. */
    private List<ListingRecord> allListings;

    @Test(priority = 1)
    public void searchForAStay() {
        log.info("Test searchForAStay - starting search with location={}, dates={} to={}, guests=[{},{},{},{}]",
                location, checkin, checkout, adults, children, infants, pets);

        results = basePage
                .setLocation(location)
                .setDates(checkin, checkout)
                .setGuests(adults, children, infants, pets)
                .search();

        allListings = results.collectAllListings();
        log.info("Collected {} listings", allListings.size());
        assertTrue(allListings.size() > 4,
                "Expected at least 5 listings but found " + allListings.size());
    }

    @Test(priority = 2, dependsOnMethods = "searchForAStay")
    public void selectTopListing() {
        log.info("Test selectTopListing - sorting listings and selecting top");
        allListings = results.sortByRatingDescThenPriceAsc(allListings);
        topListing = allListings.get(0);
        log.debug("Top listing selected: {}", topListing.url());
        Assert.assertFalse(topListing.url().isEmpty(),
                "Top listing URL should not be empty");
    }

    @Test(priority = 3, dependsOnMethods = "selectTopListing")
    public void confirmListingDetails() {
        log.info("Test confirmListingDetails - navigating to listing page");
        driver.get(topListing.url());
        listingPage = new ListingPage(driver);

        log.debug("Validating dates and guest counts on listing page");
        assertTrue(listingPage.validateDates(checkin, checkout),
                "Booking dates did not match expected values");
        assertTrue(listingPage.validateGuestsCount(adults, children, infants, pets),
                "Guest counts did not match expected values");
    }

    @Test(priority = 4, dependsOnMethods = "confirmListingDetails")
    public void adjustAndVerifyGuestCount() {
        log.info("Test adjustAndVerifyGuestCount - decreasing child guest count");
        listingPage.decreaseGuest(GuestType.CHILD, children);
        boolean passed = listingPage.validateGuestCount(GuestType.CHILD, 0);
        log.debug("Child guest count validation passed: {}", passed);
        Assert.assertTrue(passed, "Child guest count should update to 0");
    }

    @Test(priority = 5, dependsOnMethods = "adjustAndVerifyGuestCount")
    public void changeBookingDates() {
        log.info("Test changeBookingDates - shifting dates by one week");
        String newCheckin = TimeUtil.addWeeksToDate(checkin, 1);
        String newCheckout = TimeUtil.addWeeksToDate(checkout, 1);

        String originalUrl = topListing.url();
        String newUrl = UrlUtil.replaceDatesInUrl(
                originalUrl,
                TimeUtil.convertUsToIso(newCheckin),
                TimeUtil.convertUsToIso(newCheckout)
        );
        log.debug("Navigating to new URL with updated dates: {}", newUrl);
        driver.get(newUrl);
        listingPage = new ListingPage(driver);

        if (listingPage.checkIfUrlDatesAvailable()) {
            log.info("New dates available - updating internal state");
            checkin = newCheckin;
            checkout = newCheckout;
            topListing = topListing.withUrlAndDates(originalUrl, checkin, checkout);
        } else {
            log.warn("New dates not available - retaining original dates");
        }
    }

    @Test(priority = 6, dependsOnMethods = "changeBookingDates")
    public void reserveAndValidate() {
        log.info("Test reserveAndValidate - executing reservation flow");
        driver.get(topListing.url());
        listingPage = new ListingPage(driver);

        ReservationPage reservationPage = listingPage.clickReserve();
        boolean isResUrl = reservationPage.isReservationUrl();
        log.debug("isReservationUrl returned {}", isResUrl);
        Assert.assertTrue(isResUrl, "Current URL should be a reservation endpoint");

        boolean guestCountValid = reservationPage.validateGuestCountInUrl(GuestType.ADULT, adults);
        log.debug("validateGuestCountInUrl returned {}", guestCountValid);
        Assert.assertTrue(guestCountValid, "URL should contain correct adult count parameter");
    }
}