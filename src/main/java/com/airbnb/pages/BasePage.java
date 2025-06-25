package com.airbnb.pages;

import com.airbnb.utils.TimeUtil;
import com.airbnb.utils.WaitUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Month;

/**
 * BasePage encapsulates shared behavior and elements of the Airbnb search menu,
 * which appears on all primary pages within the application.
 * <p>
 * Provides methods to interact with the location input, date picker, guest selector,
 * and to execute the primary search action.
 * </p>
 */
public class BasePage {

    private static final Logger log = LoggerFactory.getLogger(BasePage.class);

    /**
     * The WebDriver instance used to perform browser interactions.
     */
    protected final WebDriver driver;

    // ───────────────────────────────────────────────────────────
    // Search menu: Location input
    // ───────────────────────────────────────────────────────────

    /**
     * Text input field for specifying the desired location in the search menu.
     */
    @FindBy(css = "input#bigsearch-query-location-input")
    private WebElement whereInput;

    // ───────────────────────────────────────────────────────────
    // Search menu: Date picker controls
    // ───────────────────────────────────────────────────────────

    @FindBy(xpath = "//div[normalize-space(text())='Add dates']")
    private WebElement datePickerToggle;
    @FindBy(css = "button[aria-label='Move forward to switch to the next month.']")
    private WebElement nextMonthButton;

    // ───────────────────────────────────────────────────────────
    // Search menu: Guest picker controls
    // ───────────────────────────────────────────────────────────

    @FindBy(xpath = "//div[normalize-space(text())='Add guests']")
    private WebElement guestsToggle;
    @FindBy(css = "button[data-testid='stepper-adults-increase-button']")
    private WebElement incAdult;
    @FindBy(css = "button[data-testid='stepper-children-increase-button']")
    private WebElement incChild;
    @FindBy(css = "button[data-testid='stepper-infants-increase-button']")
    private WebElement incInfant;
    @FindBy(css = "button[data-testid='stepper-pets-increase-button']")
    private WebElement incPets;

    // ───────────────────────────────────────────────────────────
    // Search menu: Submit button
    // ───────────────────────────────────────────────────────────

    @FindBy(css = "button[data-testid='structured-search-input-search-button']")
    private WebElement searchButton;

    /**
     * Initializes the page elements and waits for the page to fully load.
     *
     * @param driver the WebDriver instance controlling the browser
     */
    public BasePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        WaitUtil.waitForPageLoad();
        log.info("Initialized BasePage at URL: {}", driver.getCurrentUrl());
    }

    /**
     * Clicks the search button and returns a new SearchResultsPage.
     *
     * @return a fresh instance of SearchResultsPage representing the results
     */
    public SearchResultsPage search() {
        log.info("Executing search with current parameters");
        WaitUtil.waitForClickable(searchButton).click();
        return new SearchResultsPage(driver);
    }

    // ───────────────────────────────────────────────────────────
    // Location picker logic
    // ───────────────────────────────────────────────────────────

    /**
     * Sets the search location by typing the provided text into the location input.
     * Retries up to two times to handle potential stale or timing issues.
     *
     * @param location free-text location string (e.g., "Tel Aviv")
     * @return this BasePage for method chaining
     */
    public BasePage setLocation(String location) {
        log.debug("Setting location to {}", location);
        int attempts = 0;
        while (attempts < 2) {
            try {
                WaitUtil.waitForClickable(whereInput).click();
                WaitUtil.waitForVisible(whereInput).clear();
                WaitUtil.waitForVisible(whereInput).sendKeys(location);
                break;
            } catch (StaleElementReferenceException | TimeoutException ignored) {
                log.warn("Retrying setLocation due to exception (attempt {})", attempts + 1);
            }
            attempts++;
        }
        return this;
    }

    // ───────────────────────────────────────────────────────────
    // Guest picker logic
    // ───────────────────────────────────────────────────────────

    /**
     * Configures the number of guests by opening the guest selector and
     * incrementing counts for each category.
     *
     * @param adults   number of adult guests
     * @param children number of child guests
     * @param infants  number of infant guests
     * @param pets     number of pets
     * @return this BasePage for method chaining
     */
    public BasePage setGuests(int adults, int children, int infants, int pets) {
        log.debug("Setting guests: adults={}, children={}, infants={}, pets={}", adults, children, infants, pets);
        WaitUtil.waitForClickable(guestsToggle).click();
        clickMultiple(incAdult, adults);
        clickMultiple(incChild, children);
        clickMultiple(incInfant, infants);
        clickMultiple(incPets, pets);
        return this;
    }

    /**
     * Clicks the given element the specified number of times.
     * Logs each click for traceability.
     *
     * @param element the WebElement to click
     * @param times   how many times to perform the click action
     */
    private void clickMultiple(WebElement element, int times) {
        for (int i = 0; i < times; i++) {
            log.trace("Clicking element {} (iteration {}/{})", element, i + 1, times);
            WaitUtil.waitForClickable(element).click();
        }
    }

    // ───────────────────────────────────────────────────────────
    // Date picker logic
    // ───────────────────────────────────────────────────────────

    /**
     * Opens the date picker and selects both check-in and check-out dates.
     * Dates must be provided in US format (MM/dd/yyyy).
     *
     * @param checkin  check-in date string (MM/dd/yyyy)
     * @param checkout check-out date string (MM/dd/yyyy)
     * @return this BasePage for method chaining
     */
    public BasePage setDates(String checkin, String checkout) {
        log.debug("Setting dates: checkin={}, checkout={}", checkin, checkout);
        openDatePicker();
        selectDate(checkin);
        selectDate(checkout);
        return this;
    }

    /**
     * Opens the date picker panel, retrying if the control is not immediately available.
     */
    private void openDatePicker() {
        log.trace("Opening date picker panel");
        WaitUtil.waitForClickable(datePickerToggle).click();
        try {
            WaitUtil.waitForClickable(nextMonthButton);
        } catch (TimeoutException | NoSuchElementException ignore) {
            log.warn("Date picker did not open on first try, retrying");
            WaitUtil.waitForClickable(datePickerToggle).click();
        }
    }

    /**
     * Selects a specific date in the picker by navigating to the correct month/year,
     * then clicking the day button.
     *
     * @param usDate date string in US format (MM/dd/yyyy)
     */
    private void selectDate(String usDate) {
        log.trace("Selecting date {} in date picker", usDate);
        String[] parts = usDate.split("/");
        int month = Integer.parseInt(parts[0]);
        String monthName = Month.of(month).name().charAt(0)
                + Month.of(month).name().substring(1).toLowerCase();
        String monthYear = monthName + " " + parts[2];

        String expectedYearMonthSelector = "//h2[normalize-space(text())='" + monthYear + "']";
        String dayButtonSelector = "button[data-state--date-string='"
                + TimeUtil.convertUsToIso(usDate) + "']";

        while (driver.findElements(By.xpath(expectedYearMonthSelector)).isEmpty()) {
            log.trace("Current month not {} yet, clicking next month", monthYear);
            WaitUtil.waitForClickable(nextMonthButton).click();
        }

        WebElement dayBtn = WaitUtil.waitForClickable(By.cssSelector(dayButtonSelector));
        dayBtn.click();
    }
}
