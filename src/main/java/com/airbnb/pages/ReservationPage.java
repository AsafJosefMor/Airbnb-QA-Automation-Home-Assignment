package com.airbnb.pages;

import com.airbnb.enums.Endpoints;
import com.airbnb.enums.GuestType;
import com.airbnb.model.ListingRecord;
import com.airbnb.utils.UrlUtil;
import com.airbnb.utils.WaitUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Page object representing the reservation confirmation page for a specific listing.
 * <p>
 * Provides methods to validate that the URL contains correct reservation parameters,
 * including guest counts and the correct endpoint path.
 * </p>
 */
public class ReservationPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(ReservationPage.class);

    /**
     * Constructs the page by initializing WebElements and ensuring the page is loaded.
     *
     * @param driver the WebDriver instance navigating the browser
     */
    public ReservationPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
        WaitUtil.waitForPageLoad();
        log.info("Loaded ReservationPage for URL: {}", driver.getCurrentUrl());
    }

    /**
     * Validates that the current URL contains the expected guest count for a given type.
     * <p>
     * Parses the URL into a ListingRecord model to extract individual query parameters.
     * </p>
     *
     * @param guestType     the category of guest to verify (ADULT, CHILD, INFANT, PET)
     * @param expectedCount the expected number present in the URL parameters
     * @return true if the URL parameter matches expectedCount; false otherwise
     */
    public boolean validateGuestCountInUrl(GuestType guestType, int expectedCount) {
        String fullUrl = driver.getCurrentUrl();
        log.debug("Validating guest count in URL for {}: expected={}, url={}", guestType, expectedCount, fullUrl);
        ListingRecord listingRecord = UrlUtil.parseUrl(fullUrl);
        boolean result = switch (guestType) {
            case ADULT -> listingRecord.adults() == expectedCount;
            case CHILD -> listingRecord.children() == expectedCount;
            case INFANT -> listingRecord.infants() == expectedCount;
            case PET -> listingRecord.pets() == expectedCount;
        };
        log.debug("Guest count in URL validation result for {}: {}", guestType, result);
        return result;
    }

    /**
     * Checks if the current URL is a valid reservation endpoint URL.
     * <p>
     * Uses java.net.URL to extract the path and compares it against the configured
     * reservation endpoint prefix defined in {@link Endpoints}.
     * </p>
     *
     * @return true if URL path starts with "/book/stays"; false for malformed URLs or mismatches
     */
    public boolean isReservationUrl() {
        String fullUrl = driver.getCurrentUrl();
        log.debug("Checking if URL is reservation endpoint: {}", fullUrl);
        try {
            URL url = new URL(fullUrl);
            String path = url.getPath();
            String reservationPath = Endpoints.RESERVATION.getPath();
            boolean result = path != null && path.startsWith(reservationPath);
            log.debug("isReservationUrl result: {} (path={})", result, path);
            return result;
        } catch (MalformedURLException e) {
            log.error("Malformed URL encountered: {}", fullUrl, e);
            return false;
        }
    }
}