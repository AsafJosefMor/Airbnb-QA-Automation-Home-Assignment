package com.airbnb.pages;

import com.airbnb.enums.GuestType;
import com.airbnb.utils.WaitUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page object representing an individual Airbnb listing's detail page.
 * <p>
 * Provides accessors for key attributes (name, price, rating, dates) and actions
 * (reserving, adjusting guest counts) on the listing page.
 * </p>
 */
public class ListingPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(ListingPage.class);

    // ────────────────────────────────────────────────────────────────────────────
    // Elements displaying listing information
    // ────────────────────────────────────────────────────────────────────────────

    @FindBy(css = "h1[elementtiming='LCP-target']")
    private WebElement name;
    @FindBy(xpath = "//span[contains(text(),' per night')]")
    private WebElement pricePerNight;
    @FindBy(xpath = "//div[@aria-hidden='true' and normalize-space(text()) and contains(text(),'.')]")
    private WebElement rating;
    @FindBy(css = "div[data-testid='change-dates-checkIn']")
    private WebElement checkinDate;
    @FindBy(css = "div[data-testid='change-dates-checkOut']")
    private WebElement checkoutDate;

    // ────────────────────────────────────────────────────────────────────────────
    // Guest Picker Elements
    // ────────────────────────────────────────────────────────────────────────────

    @FindBy(id = "GuestPicker-book_it-trigger")
    private WebElement guestPicker;
    @FindBy(css = "span[data-testid='GuestPicker-book_it-form-adults-stepper-value']")
    private WebElement adultCountGuestPicker;
    @FindBy(css = "span[data-testid='GuestPicker-book_it-form-children-stepper-value']")
    private WebElement childCountGuestPicker;
    @FindBy(css = "span[data-testid='GuestPicker-book_it-form-infants-stepper-value']")
    private WebElement infantCountGuestPicker;
    @FindBy(css = "span[data-testid='GuestPicker-book_it-form-pets-stepper-value']")
    private WebElement petCountGuestPicker;
    @FindBy(css = "button[data-testid='GuestPicker-book_it-form-children-stepper-decrease-button']")
    private WebElement decreaseChildButton;

    // ────────────────────────────────────────────────────────────────────────────
    // Reservation action elements and error messages
    // ────────────────────────────────────────────────────────────────────────────

    @FindBy(css = "button[data-testid='homes-pdp-cta-btn']")
    private WebElement reserveButton;
    @FindBy(css = "#site-content > div > div:nth-child(1) > div:nth-child(3) > div > div > div > div" +
            " > div:nth-child(1) > div > div > div > div > div > div > div > div > div > button")
    private WebElement reserveButtonFallback;
    @FindBy(id = "bookItTripDetailsError")
    private WebElement datesError;
    @FindBy(css = "button[aria-label='Close']")
    private WebElement translationPopUpCloseButtonSelector;

    /**
     * Initializes the listing page, sets up elements, waits for load, and
     * closes any translation pop-up that may obstruct interactions.
     *
     * @param driver the WebDriver instance controlling the browser
     */
    public ListingPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
        WaitUtil.waitForPageLoad();
        log.info("Loaded ListingPage for URL: {}", driver.getCurrentUrl());
        closeTranslationPopUp();
    }

    /**
     * Attempts to click the "Reserve" button to navigate to the reservation page.
     * Falls back to an alternate locator if the primary fails.
     *
     * @return a new ReservationPage representing the next flow
     */
    public ReservationPage clickReserve() {
        log.debug("Attempting to click Reserve button");
        try {
            WaitUtil.waitForClickable(reserveButton).click();
        } catch (TimeoutException | NoSuchElementException e) {
            log.warn("Primary reserve button not found, trying fallback");
            WaitUtil.waitForClickable(reserveButtonFallback).click();
        }
        return new ReservationPage(driver);
    }

    /**
     * Validates that the check-in and check-out dates displayed on the page
     * match the expected values.
     *
     * @param expectedCheckin  expected check-in date (e.g. "07/24/2025")
     * @param expectedCheckout expected check-out date (e.g. "07/27/2025")
     * @return true if both dates match; false otherwise
     */
    public boolean validateDates(String expectedCheckin, String expectedCheckout) {
        log.trace("Validating dates: expected={} / {}", expectedCheckin, expectedCheckout);
        boolean result = checkinDate.getText().equals(expectedCheckin)
                && checkoutDate.getText().equals(expectedCheckout);
        log.debug("Date validation result: {}", result);
        return result;
    }

    /**
     * Decreases the count of a specified GuestType by clicking the "-" button
     * the given number of times, reopening and closing the guest picker as needed.
     *
     * @param guestType    the category of guest to decrease (only CHILD implemented)
     * @param decreaseCount number of times to click the decrease button
     */
    public void decreaseGuest(GuestType guestType, int decreaseCount) {
        log.debug("Decreasing guest count for {} by {}", guestType, decreaseCount);
        guestPicker.click();
        WebElement element;
        switch (guestType) {
            case CHILD:
                element = decreaseChildButton;
                break;
            default:
                throw new UnsupportedOperationException("Decrease not implemented for " + guestType);
        }
        for (int i = 0; i < decreaseCount; i++) {
            log.trace("Clicking decrease for {} (iteration {}/{})", guestType, i+1, decreaseCount);
            WaitUtil.sleep(100);
            WaitUtil.waitForClickable(element).click();
        }
        guestPicker.click();
    }

    /**
     * Validates that the current guest counts displayed in the picker
     * match the expected passengers for all categories.
     *
     * @param adults   expected adult count
     * @param children expected child count
     * @param infants  expected infant count
     * @param pets     expected pet count
     * @return true if all displayed counts match; false otherwise
     */
    public boolean validateGuestsCount(int adults, int children, int infants, int pets) {
        log.trace("Validating all guest counts: adults={}, children={}, infants={}, pets={}", adults, children, infants, pets);
        guestPicker.click();
        int adultsInListing = Integer.parseInt(WaitUtil.waitForVisible(adultCountGuestPicker).getText());
        int childrenInListing = Integer.parseInt(WaitUtil.waitForVisible(childCountGuestPicker).getText());
        int infantsInListing = Integer.parseInt(WaitUtil.waitForVisible(infantCountGuestPicker).getText());
        int petsInListing = Integer.parseInt(WaitUtil.waitForVisible(petCountGuestPicker).getText());
        guestPicker.click();
        boolean result = adultsInListing==adults && childrenInListing==children && infantsInListing==infants && petsInListing==pets;
        log.debug("Guest count validation result: {}", result);
        return result;
    }

    /**
     * Validates a single GuestType count by opening the picker, reading the
     * displayed value, and closing the picker.
     *
     * @param guestType     the guest category to validate
     * @param expectedCount the expected number for that category
     * @return true if displayed count matches expected; false otherwise
     */
    public boolean validateGuestCount(GuestType guestType, int expectedCount) {
        log.trace("Validating guest count for {}: expected={}", guestType, expectedCount);
        guestPicker.click();
        WebElement selector = switch (guestType) {
            case ADULT -> adultCountGuestPicker;
            case CHILD -> childCountGuestPicker;
            case INFANT -> infantCountGuestPicker;
            case PET -> petCountGuestPicker;
        };
        int counter = Integer.parseInt(WaitUtil.waitForVisible(selector).getText());
        guestPicker.click();
        boolean result = counter==expectedCount;
        log.debug("Single guest count validation result for {}: {}", guestType, result);
        return result;
    }

    /**
     * Determines whether the page URL parameters for dates are valid (no error shown).
     *
     * @return true if no date error is visible; false otherwise
     */
    public boolean checkIfUrlDatesAvailable() {
        log.trace("Checking for date error on page");
        try {
            WaitUtil.waitForVisible(datesError);
            log.debug("Date error element found");
            return false;
        } catch (NoSuchElementException | TimeoutException ignore) {
            log.debug("No date error present");
        }
        return true;
    }

    /**
     * Attempts to close a translation pop-up overlay if it appears,
     * logging the outcome.
     */
    private void closeTranslationPopUp() {
        try {
            WaitUtil.waitForClickable(translationPopUpCloseButtonSelector).click();
            log.info("Closed translation pop-up.");
        } catch (TimeoutException | NoSuchElementException ignore) {
            log.info("No translation pop-up appeared.");
        }
    }
}