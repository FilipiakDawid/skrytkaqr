package io.github.filipiakdawid.skrytkaqr.unit.util.smsParser

import io.github.filipiakdawid.skrytkaqr.util.AppPreferences
import io.github.filipiakdawid.skrytkaqr.util.SmsParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsParserTest : SmsParserTrait {
    private val defaultCodeRegex = AppPreferences.Companion.DEFAULT_CODE_REGEX
    private val defaultLockerRegex = AppPreferences.Companion.DEFAULT_LOCKER_REGEX

    /*
     * @feature Sms parsing
     * @scenario Parse standard SMS
     * @case Provide valid standard SMS
     */
    @Test
    fun parse_provideValidStandardSms_returnParseResult() {
        // GIVEN
        val body = smsStandard()

        // WHEN
        val result = SmsParser.parse(body, defaultCodeRegex, defaultLockerRegex)

        // THEN
        assertNotNull(result)
        assertEquals("905432", result!!.code)
        assertEquals("WWA35M Warszawa", result.location)
        assertEquals("16/04/2026", result.expiryDate)
        assertEquals("09:31", result.expiryTime)
        assertEquals(1, result.parcelCount)
    }

    /*
     * @feature Sms parsing
     * @scenario Parse SMS without locker keyword
     * @case Provide SMS without "skrytka/paczkomat" word
     */
    @Test
    fun parse_provideSmsWithoutLockerKeyword_returnNull() {
        // GIVEN
        val body = "Twoja paczka jest w drodze. Kod odbioru 905432."

        // WHEN
        val result = SmsParser.parse(body, defaultCodeRegex, defaultLockerRegex)

        // THEN
        assertNull(result)
    }

    /*
     * @feature Sms parsing
     * @scenario Parse SMS without pickup code
     * @case Provide SMS without code
     */
    @Test
    fun parse_provideSmsWithoutCode_returnNull() {
        // GIVEN
        val body = "Paczka czeka w skrytce w WWA35M Warszawa. Odbierz ją jak najszybciej."

        // WHEN
        val result = SmsParser.parse(body, defaultCodeRegex, defaultLockerRegex)

        // THEN
        assertNull(result)
    }

    /*
     * @feature Sms parsing
     * @scenario Parse SMS with invalid code regex
     * @case Provide invalid codeRegex
     */
    @Test
    fun parse_provideInvalidCodeRegex_returnNull() {
        // GIVEN
        val body = smsStandard()
        val invalidRegex = "["

        // WHEN
        val result = SmsParser.parse(body, invalidRegex, defaultLockerRegex)

        // THEN
        assertNull(result)
    }

    /*
     * @feature Sms parsing
     * @scenario Parse SMS with invalid locker regex
     * @case Provide invalid lockerRegex
     */
    @Test
    fun parse_provideInvalidLockerRegex_returnNull() {
        // GIVEN
        val body = smsStandard()
        val invalidRegex = "["

        // WHEN
        val result = SmsParser.parse(body, defaultCodeRegex, invalidRegex)

        // THEN
        assertNull(result)
    }

    /*
     * @feature Sms parsing
     * @scenario Parse SMS with parcel number
     * @case Provide SMS containing 18-digit parcel number
     */
    @Test
    fun parse_provideSmsWithParcelNumber_returnParcelNumber() {
        // GIVEN
        val body = smsWithParcelNumber()

        // WHEN
        val result = SmsParser.parse(body, defaultCodeRegex, defaultLockerRegex)

        // THEN
        assertNotNull(result)
        assertEquals("123456789012345678", result!!.parcelNumber)
    }

    /*
     * @feature Sms parsing
     * @scenario Parse SMS without expiry date
     * @case Provide SMS without date
     */
    @Test
    fun parse_provideSmsWithoutExpiryDate_returnEmptyExpiryDate() {
        // GIVEN
        val body = smsNoDate()

        // WHEN
        val result = SmsParser.parse(body, defaultCodeRegex, defaultLockerRegex)

        // THEN
        assertNotNull(result)
        assertEquals("", result!!.expiryDate)
        assertEquals("", result.expiryTime)
    }

    /*
     * @feature Sms parsing
     * @scenario Parse SMS with expiry time
     * @case Provide SMS with date and time
     */
    @Test
    fun parse_provideSmsWithExpiryTime_returnExpiryTime() {
        // GIVEN
        val body = smsStandard()

        // WHEN
        val result = SmsParser.parse(body, defaultCodeRegex, defaultLockerRegex)

        // THEN
        assertNotNull(result)
        assertEquals("09:31", result!!.expiryTime)
    }

    /*
     * @feature Sms parsing
     * @scenario Parse SMS with date but no time
     * @case Provide SMS with date only
     */
    @Test
    fun parse_provideSmsWithDateOnly_returnEmptyExpiryTime() {
        // GIVEN
        val body = smsWithParcelNumber()

        // WHEN
        val result = SmsParser.parse(body, defaultCodeRegex, defaultLockerRegex)

        // THEN
        assertNotNull(result)
        assertEquals("16/04/2026", result!!.expiryDate)
        assertEquals("", result.expiryTime)
    }

    /*
     * @feature Sms parsing
     * @scenario Parse empty SMS body
     * @case Provide empty string
     */
    @Test
    fun parse_provideEmptyBody_returnNull() {
        // GIVEN
        val body = ""

        // WHEN
        val result = SmsParser.parse(body, defaultCodeRegex, defaultLockerRegex)

        // THEN
        assertNull(result)
    }

    /*
     * @feature Sms parsing
     * @scenario Parse SMS with uppercase locker keyword
     * @case Provide SMS with SKRYTCE uppercase - lockerRegex case-insensitive
     */
    @Test
    fun parse_provideUppercaseLockerKeyword_returnParseResult() {
        // GIVEN
        val body = "Paczka czeka W SKRYTCE w WWA35M Warszawa. Kod odbioru 905432."

        // WHEN
        val result = SmsParser.parse(body, defaultCodeRegex, defaultLockerRegex)

        // THEN
        assertNotNull(result)
        assertEquals("905432", result!!.code)
    }

    /*
     * @feature Sms parsing
     * @scenario Parse multi-locker SMS
     * @case Provide multi-locker SMS with "Paczkomat" location format
     */
    @Test
    fun parse_provideMultiLockerSms_returnLocationFromPaczkomat() {
        // GIVEN
        val body = smsMultiLocker()

        // WHEN
        val result = SmsParser.parse(body, defaultCodeRegex, defaultLockerRegex)

        // THEN
        assertNotNull(result)
        assertEquals("WWA01M Warszawa", result!!.location)
    }

    /*
     * @feature Sms parsing
     * @scenario Parse multi-locker SMS
     * @case Provide multi-locker SMS - parcelCount should be 2
     */
    @Test
    fun parse_provideMultiLockerSms_returnParcelCountTwo() {
        // GIVEN
        val body = smsMultiLocker()

        // WHEN
        val result = SmsParser.parse(body, defaultCodeRegex, defaultLockerRegex)

        // THEN
        assertNotNull(result)
        assertEquals(2, result!!.parcelCount)
    }

    /*
     * @feature Sms parsing
     * @scenario Parse multi-locker SMS
     * @case Provide multi-locker SMS - expiryDate and expiryTime filled
     */
    @Test
    fun parse_provideMultiLockerSms_returnExpiryDateAndTime() {
        // GIVEN
        val body = smsMultiLocker()

        // WHEN
        val result = SmsParser.parse(body, defaultCodeRegex, defaultLockerRegex)

        // THEN
        assertNotNull(result)
        assertEquals("02/05/2026", result!!.expiryDate)
        assertEquals("09:02", result.expiryTime)
    }

    /*
     * @feature Sms parsing
     * @scenario Parse standard SMS
     * @case Provide standard SMS - parcelCount should be 1
     */
    @Test
    fun parse_provideStandardSms_returnParcelCountOne() {
        // GIVEN
        val body = smsStandard()

        // WHEN
        val result = SmsParser.parse(body, defaultCodeRegex, defaultLockerRegex)

        // THEN
        assertNotNull(result)
        assertEquals(1, result!!.parcelCount)
    }

    /*
     * @feature Sms parsing
     * @scenario Parse multi-locker SMS with colon in count
     * @case Provide SMS with "Liczba paczek: 3" with colon
     */
    @Test
    fun parse_provideSmsWithColonInPackageCount_returnParcelCountThree() {
        // GIVEN
        val body =
            "W Twojej skrytce jest wiecej paczek. Liczba paczek: 3. " +
                "Paczkomat WWA01M Warszawa, Warszawa 515A. " +
                "Kod odbioru 133713. Czas odbioru do 02/05/2026 09:02."

        // WHEN
        val result = SmsParser.parse(body, defaultCodeRegex, defaultLockerRegex)

        // THEN
        assertNotNull(result)
        assertEquals(3, result!!.parcelCount)
    }

    /*
     * @feature Sms parsing
     * @scenario Test code regex
     * @case Provide valid regex with capture group
     */
    @Test
    fun testCodeRegex_provideValidRegexWithGroup_returnCode() {
        // GIVEN
        val regex = defaultCodeRegex
        val sample = smsStandard()

        // WHEN
        val result = SmsParser.testCodeRegex(regex, sample)

        // THEN
        assertEquals("905432", result)
    }

    /*
     * @feature Sms parsing
     * @scenario Test code regex
     * @case Provide regex without capture group
     */
    @Test
    fun testCodeRegex_provideRegexWithoutCaptureGroup_returnNull() {
        // GIVEN
        val regex = """\d{6}"""
        val sample = smsStandard()

        // WHEN
        val result = SmsParser.testCodeRegex(regex, sample)

        // THEN
        assertNull(result)
    }

    /*
     * @feature Sms parsing
     * @scenario Test locker regex
     * @case Provide matching text
     */
    @Test
    fun testLockerRegex_provideMatchingText_returnTrue() {
        // GIVEN
        val regex = defaultLockerRegex
        val sample = smsStandard()

        // WHEN
        val result = SmsParser.testLockerRegex(regex, sample)

        // THEN
        assertTrue(result)
    }

    /*
     * @feature Sms parsing
     * @scenario Test locker regex
     * @case Provide non-matching text
     */
    @Test
    fun testLockerRegex_provideNonMatchingText_returnFalse() {
        // GIVEN
        val regex = defaultLockerRegex
        val sample = "Twoja paczka jest w drodze. Odbierz ją jutro."

        // WHEN
        val result = SmsParser.testLockerRegex(regex, sample)

        // THEN
        assertFalse(result)
    }

    /*
     * @feature Sms parsing
     * @scenario Validate regex pattern
     * @case Provide valid regex
     */
    @Test
    fun isValidRegex_provideValidRegex_returnTrue() {
        // GIVEN
        val pattern = defaultCodeRegex

        // WHEN
        val result = SmsParser.isValidRegex(pattern)

        // THEN
        assertTrue(result)
    }

    /*
     * @feature Sms parsing
     * @scenario Validate regex pattern
     * @case Provide invalid regex with unclosed bracket
     */
    @Test
    fun isValidRegex_provideInvalidRegex_returnFalse() {
        // GIVEN
        val pattern = "["

        // WHEN
        val result = SmsParser.isValidRegex(pattern)

        // THEN
        assertFalse(result)
    }

    /*
     * @feature Sms parsing
     * @scenario Parse LockerFull SMS
     * @case Provide LockerFull SMS body - return all fields parsed correctly
     */
    @Test
    fun parse_provideLockerFullSms_returnAllFieldsParsedCorrectly() {
        // GIVEN
        val body = smsLockerFull()

        // WHEN
        val result = SmsParser.parse(body, defaultCodeRegex, defaultLockerRegex)

        // THEN
        assertNotNull(result)
        assertEquals("937245", result!!.code)
        assertEquals("POZ82M Sniadeckich 40", result.location)
        assertEquals("06/11/2020", result.expiryDate)
        assertEquals("", result.expiryTime)
        assertEquals("632625396060813017651337", result.parcelNumber)
        assertEquals(1, result.parcelCount)
    }
}
