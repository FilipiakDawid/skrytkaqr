package io.github.filipiakdawid.skrytkaqr.unit.ui.parcelUiModel

import io.github.filipiakdawid.skrytkaqr.ui.ExpiryState
import io.github.filipiakdawid.skrytkaqr.ui.toUiModel
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class ParcelUiModelTest : ParcelUiModelTrait {
    private val fixedNow = LocalDateTime.of(2026, 5, 2, 10, 0)

    /*
     * @feature Parcel
     * @scenario Resolve expiry state
     * @case Provide parcel without expiry date
     */
    @Test
    fun toUiModel_provideParcelWithoutExpiryDate_returnNormal() {
        // GIVEN
        val parcel = buildParcel(expiryDate = "", expiryTime = "")

        // WHEN
        val result = parcel.toUiModel(now = fixedNow)

        // THEN
        assertEquals(ExpiryState.NORMAL, result.expiryState)
    }

    /*
     * @feature Parcel
     * @scenario Resolve expiry state
     * @case Provide future expiry date without time
     */
    @Test
    fun toUiModel_provideFutureDateWithoutTime_returnNormal() {
        // GIVEN
        val parcel = buildParcel(expiryDate = "10/05/2026", expiryTime = "")

        // WHEN
        val result = parcel.toUiModel(now = fixedNow)

        // THEN
        assertEquals(ExpiryState.NORMAL, result.expiryState)
    }

    /*
     * @feature Parcel
     * @scenario Resolve expiry state
     * @case Provide today's expiry date without time
     */
    @Test
    fun toUiModel_provideTodayDateWithoutTime_returnNormal() {
        // GIVEN
        val parcel = buildParcel(expiryDate = "02/05/2026", expiryTime = "")

        // WHEN
        val result = parcel.toUiModel(now = fixedNow)

        // THEN
        assertEquals(ExpiryState.NORMAL, result.expiryState)
    }

    /*
     * @feature Parcel
     * @scenario Resolve expiry state
     * @case Provide yesterday's expiry date without time
     */
    @Test
    fun toUiModel_provideYesterdayDateWithoutTime_returnExpired() {
        // GIVEN
        val parcel = buildParcel(expiryDate = "01/05/2026", expiryTime = "")

        // WHEN
        val result = parcel.toUiModel(now = fixedNow)

        // THEN
        assertEquals(ExpiryState.EXPIRED, result.expiryState)
    }

    /*
     * @feature Parcel
     * @scenario Resolve expiry state
     * @case Provide expiry date and time with more than 8 hours left
     */
    @Test
    fun toUiModel_provideDateAndTimeMoreThan8HoursLeft_returnNormal() {
        // GIVEN - now = 10:00, expiry = 20:00 (10h left)
        val parcel = buildParcel(expiryDate = "02/05/2026", expiryTime = "20:00")

        // WHEN
        val result = parcel.toUiModel(now = fixedNow)

        // THEN
        assertEquals(ExpiryState.NORMAL, result.expiryState)
    }

    /*
     * @feature Parcel
     * @scenario Resolve expiry state
     * @case Provide expiry date and time with less than 8 hours left
     */
    @Test
    fun toUiModel_provideDateAndTimeLessThan8HoursLeft_returnWarning() {
        // GIVEN - now = 10:00, expiry = 14:00 (4h left)
        val parcel = buildParcel(expiryDate = "02/05/2026", expiryTime = "14:00")

        // WHEN
        val result = parcel.toUiModel(now = fixedNow)

        // THEN
        assertEquals(ExpiryState.WARNING, result.expiryState)
    }

    /*
     * @feature Parcel
     * @scenario Resolve expiry state
     * @case Provide expiry date and time already passed
     */
    @Test
    fun toUiModel_provideDateAndTimeAlreadyPassed_returnExpired() {
        // GIVEN - now = 10:00, expiry = 09:00 (1h ago)
        val parcel = buildParcel(expiryDate = "02/05/2026", expiryTime = "09:00")

        // WHEN
        val result = parcel.toUiModel(now = fixedNow)

        // THEN
        assertEquals(ExpiryState.EXPIRED, result.expiryState)
    }

    /*
     * @feature Parcel
     * @scenario Resolve expiry state
     * @case Provide invalid date format
     */
    @Test
    fun toUiModel_provideInvalidDateFormat_returnNormal() {
        // GIVEN
        val parcel = buildParcel(expiryDate = "invalid-date", expiryTime = "")

        // WHEN
        val result = parcel.toUiModel(now = fixedNow)

        // THEN
        assertEquals(ExpiryState.NORMAL, result.expiryState)
    }

    /*
     * @feature Parcel
     * @scenario Resolve expiry state
     * @case Provide valid date but invalid time format
     */
    @Test
    fun toUiModel_provideValidDateButInvalidTimeFormat_returnNormal() {
        // GIVEN
        val parcel = buildParcel(expiryDate = "02/05/2026", expiryTime = "invalid-time")

        // WHEN
        val result = parcel.toUiModel(now = fixedNow)

        // THEN
        assertEquals(ExpiryState.NORMAL, result.expiryState)
    }

    /*
     * @feature Parcel
     * @scenario Resolve expiry state
     * @case Provide expiry exactly 8 hours from now - boundary condition
     */
    @Test
    fun toUiModel_provideDateAndTimeExactly8HoursLeft_returnNormal() {
        // GIVEN - now = 10:00, expiry = 18:00 (exactly 8h)
        val parcel = buildParcel(expiryDate = "02/05/2026", expiryTime = "18:00")

        // WHEN
        val result = parcel.toUiModel(now = fixedNow)

        // THEN
        assertEquals(ExpiryState.NORMAL, result.expiryState)
    }

    /*
     * @feature Parcel
     * @scenario Resolve expiry state
     * @case Provide expiry exactly now (hoursLeft == 0) - boundary condition
     */
    @Test
    fun toUiModel_provideDateAndTimeExactlyNow_returnExpired() {
        // GIVEN - now = 10:00, expiry = 10:00 (0h left)
        val parcel = buildParcel(expiryDate = "02/05/2026", expiryTime = "10:00")

        // WHEN
        val result = parcel.toUiModel(now = fixedNow)

        // THEN
        assertEquals(ExpiryState.EXPIRED, result.expiryState)
    }
}
