package com.airbnb.enums;

/**
 * Enumeration of guest categories for Airbnb reservations.
 * <p>
 * Represents the different types of guests that can be specified when booking:
 * </p>
 * <ul>
 *   <li>{@link #ADULT} — Individuals aged 18 and over.</li>
 *   <li>{@link #CHILD} — Children between ages 2 and 17.</li>
 *   <li>{@link #INFANT} — Infants under age 2.</li>
 *   <li>{@link #PET} — Pets accompanying the guests.</li>
 * </ul>
 * <p>
 * This enum is used to parameterize search queries and validate booking payloads
 * to ensure correct formatting of guest counts.
 * </p>
 */
public enum GuestType {
    ADULT,
    CHILD,
    INFANT,
    PET
}
