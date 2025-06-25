package com.airbnb.pages;

import com.airbnb.model.ListingRecord;
import com.airbnb.utils.WaitUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Page object for Airbnb search results pages.
 * <p>
 * Provides functionality to collect listing summaries across all result pages,
 * navigate through pagination, and sort the collected listings.
 * </p>
 */
public class SearchResultsPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(SearchResultsPage.class);

    /**
     * Accumulates ListingRecord objects for all listings encountered.
     */
    private List<ListingRecord> results = new ArrayList<>();

    /**
     * "Next" button element for pagination.
     */
    @FindBy(css = "a[aria-label='Next']")
    private WebElement nextPageButton;

    // Locators for individual listing card components
    private static final String listingCardSelector = "//div[@data-testid='card-container']";
    private static final String nameSelector = "span[data-testid='listing-card-name']";
    private static final String pricePerNightSelector = ".//span[contains(text(),' per night')]";
    private static final String totalPriceSelector = ".//span[contains(text(),' total')]";
    private static final String ratingSelector = "//span[contains(., 'average rating')]";
    private static final String ratingFallbackSelector = ".//span[@aria-hidden='true' and contains(text(),' (')]";
    private static final String urlSelector = "a";

    /**
     * Initializes elements and ensures the page is fully loaded.
     *
     * @param driver the WebDriver instance driving the browser
     */
    public SearchResultsPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
        WaitUtil.waitForPageLoad();
        log.info("SearchResultsPage initialized at URL: {}", driver.getCurrentUrl());
    }

    /**
     * Iterates through all search result pages, collects listing data,
     * and returns a list of ListingRecord instances.
     * <p>
     * Stops pagination when no "Next" button is available.
     * </p>
     *
     * @return list of collected ListingRecord objects
     */
    public List<ListingRecord> collectAllListings() {
        log.debug("Starting to collect listings across pages");

        do {
            // Wait until at least 6 listing cards are visible
            WaitUtil.getWait().until(
                    ExpectedConditions.numberOfElementsToBeMoreThan(
                            By.xpath(listingCardSelector), 5));

            List<WebElement> listingCards = driver.findElements(
                    By.xpath(listingCardSelector));

            log.debug("Found {} listing cards on current page", listingCards.size());

            // Collect listings carefully with try-catch
            for (WebElement card : listingCards) {
                try {
                    ListingRecord record = getUrlListingFromResult(card);
                    results.add(record);
                    log.trace("Added listing: {}", record.url());
                } catch (NoSuchElementException e) {
                    log.warn("Skipping a listing card due to missing element");
                }
            }
        } while (goToNextPage());

        log.info("Collected a total of {} listings", results.size());
        return results;
    }

    /**
     * Extracts listing information from a single result card element.
     *
     * @param card the WebElement representing a result card
     * @return a ListingRecord with parsed details
     */
    private ListingRecord getUrlListingFromResult(WebElement card) {
        log.trace("Extracting listing data from card");
        WebElement urlEl = WaitUtil.waitForClickable(card.findElement(By.cssSelector(urlSelector)));
        WebElement nameEl = WaitUtil.waitForVisible(card.findElement(By.cssSelector(nameSelector)));
        WebElement priceEl = WaitUtil.waitForVisible(card.findElement(By.xpath(pricePerNightSelector)));
        WebElement totalEl = WaitUtil.waitForVisible(card.findElement(By.xpath(totalPriceSelector)));

        String url = urlEl.getAttribute("href");
        String name = nameEl.getText();
        double price = parsePrice(priceEl.getText());
        double total = parsePrice(totalEl.getText());

        // Rating might be fleaky to retrieve so it has a fallback option
        double rating;
        WebElement ratingEl;
        try {
            // Try option 1
            ratingEl = WaitUtil.waitForVisible(card.findElement(By.xpath(ratingSelector)));
            String fullText = ratingEl.getText().trim();
            String firstToken = fullText.split("\\s+")[0];
            rating = Double.parseDouble(firstToken);
        }
        catch (IllegalStateException | NoSuchElementException | TimeoutException ignore) {
            // Fallback to option 2
            log.warn("Failed retrieving rating with option 1, using fallback option 2");
            ratingEl = WaitUtil.waitForVisible(card.findElement(By.xpath(ratingFallbackSelector)));
            rating = Double.parseDouble(ratingEl.getText().split(" ")[0]);
        }

        return new ListingRecord(name, rating, price, total, url,
                null, null, null, null, null, null);
    }

    /**
     * Attempts to click the "Next" button for pagination.
     *
     * @return true if navigation occurred; false otherwise
     */
    private boolean goToNextPage() {
        log.trace("Checking for presence of Next button");
        try {
            if (WaitUtil.waitForClickable(nextPageButton).isDisplayed()) {
                log.debug("Clicking Next to go to next page");
                nextPageButton.click();
                WaitUtil.waitForPageLoad();
                return true;
            }
        } catch (TimeoutException | NoSuchElementException e) {
            log.info("No Next button found, ending pagination");
        }
        return false;
    }

    /**
     * Sorts listings by descending rating, then ascending price per night.
     *
     * @param listings the list of ListingRecord objects to sort
     * @return a new sorted list of listings
     */
    public List<ListingRecord> sortByRatingDescThenPriceAsc(List<ListingRecord> listings) {
        log.debug("Sorting {} listings by rating desc then price asc", listings.size());
        List<ListingRecord> sorted = listings.stream()
                .sorted(Comparator.comparing(ListingRecord::rating).reversed()
                        .thenComparing(ListingRecord::pricePerNight))
                .collect(Collectors.toList());
        log.trace("Sorted top listing: {}", sorted.get(0).url());
        return sorted;
    }

    /**
     * Converts a price string (with currency symbols and commas) to a double value.
     *
     * @param text the raw price text
     * @return numeric price value
     */
    private double parsePrice(String text) {
        String num = text.replaceAll("[^\\d.]", "");
        return Double.parseDouble(num);
    }
}