package io.github.filipiakdawid.skrytkaqr.unit.util.smsSyncProcessor

import io.github.filipiakdawid.skrytkaqr.data.db.ParcelDao
import io.github.filipiakdawid.skrytkaqr.util.AppPreferences
import io.github.filipiakdawid.skrytkaqr.util.SmsProcessResult
import io.github.filipiakdawid.skrytkaqr.util.SmsSyncProcessor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SmsSyncProcessorTest : SmsSyncProcessorTrait {
    private val defaultCodeRegex = AppPreferences.DEFAULT_CODE_REGEX
    private val defaultLockerRegex = AppPreferences.DEFAULT_LOCKER_REGEX

    private lateinit var dao: ParcelDao
    private lateinit var processor: SmsSyncProcessor

    @Before
    fun setUp() {
        dao = mockk(relaxed = true)
        processor = SmsSyncProcessor(dao)
    }

    /*
     * @feature Sms parsing
     * @scenario Deduplicate SMS list
     * @case Provide 3 SMS with same code and increasing parcelCount - expect 1 result
     */
    @Test
    fun process_provide3SmsWithSameCode_return1Result() =
        runTest {
            // GIVEN
            coEvery { dao.getByCode(any()) } returns null

            val rawList =
                listOf(
                    rawSms(smsBodySingle("905432"), date = 1000L),
                    rawSms(smsBodyMulti("905432", parcelCount = 2), date = 2000L),
                    rawSms(smsBodyMulti("905432", parcelCount = 3), date = 3000L),
                )

            // WHEN
            val results = processor.process(rawList, defaultCodeRegex, defaultLockerRegex)

            // THEN
            assertEquals(1, results.size)
        }

    /*
     * @feature Sms parsing
     * @scenario Deduplicate SMS list
     * @case Provide 3 SMS with same code increasing parcelCount - newest wins, parcelCount == 3
     */
    @Test
    fun process_provide3SmsWithSameCodeIncreasingCount_insertWithParcelCount3() =
        runTest {
            // GIVEN
            coEvery { dao.getByCode(any()) } returns null

            val rawList =
                listOf(
                    rawSms(smsBodySingle("905432"), date = 1000L),
                    rawSms(smsBodyMulti("905432", parcelCount = 2), date = 2000L),
                    rawSms(smsBodyMulti("905432", parcelCount = 3), date = 3000L),
                )

            // WHEN
            processor.process(rawList, defaultCodeRegex, defaultLockerRegex)

            // THEN
            coVerify {
                dao.insert(match { it.parcelCount == 3 })
            }
        }

    /*
     * @feature Sms parsing
     * @scenario Import new parcel
     * @case Provide SMS not in DB - return Imported
     */
    @Test
    fun process_provideSmsNotInDb_returnImported() =
        runTest {
            // GIVEN
            coEvery { dao.getByCode(any()) } returns null

            val rawList = listOf(provideSingleNewSms())

            // WHEN
            val results = processor.process(rawList, defaultCodeRegex, defaultLockerRegex)

            // THEN
            assertEquals(1, results.count { it == SmsProcessResult.Imported })
        }

    /*
     * @feature Sms parsing
     * @scenario Import multiple new parcels
     * @case Provide 2 SMS with different codes both not in DB - imported == 2
     */
    @Test
    fun process_provide2DifferentNewSms_returnImported2() =
        runTest {
            // GIVEN
            coEvery { dao.getByCode(any()) } returns null

            val rawList =
                listOf(
                    provideSingleNewSms(code = "905432", date = 1000L),
                    provideSingleNewSms(code = "133713", date = 2000L),
                )

            // WHEN
            val results = processor.process(rawList, defaultCodeRegex, defaultLockerRegex)

            // THEN
            assertEquals(2, results.count { it == SmsProcessResult.Imported })
        }

    /*
     * @feature Sms parsing
     * @scenario Update existing parcel
     * @case Provide SMS with existing code, parcelCount 1 to 2 - return Updated
     */
    @Test
    fun process_provideExistingCodeWithIncreasedParcelCount_returnUpdated() =
        runTest {
            // GIVEN
            coEvery { dao.getByCode("905432") } returns
                provideActiveParcel(
                    "905432",
                    parcelCount = 1,
                )

            val rawList = listOf(rawSms(smsBodyMulti("905432", parcelCount = 2)))

            // WHEN
            val results = processor.process(rawList, defaultCodeRegex, defaultLockerRegex)

            // THEN
            assertEquals(1, results.count { it == SmsProcessResult.Updated })
        }

    /*
     * @feature Sms parsing
     * @scenario Skip unchanged parcel
     * @case Provide SMS with existing code and same parcelCount - return Skipped
     */
    @Test
    fun process_provideExistingCodeWithSameParcelCount_returnSkipped() =
        runTest {
            // GIVEN
            coEvery { dao.getByCode("905432") } returns
                provideActiveParcel(
                    "905432",
                    parcelCount = 2,
                    "WWA01M Warszawa",
                    "02/05/2026",
                    "09:02",
                )

            val rawList = listOf(rawSms(smsBodyMulti("905432", parcelCount = 2)))

            // WHEN
            val results = processor.process(rawList, defaultCodeRegex, defaultLockerRegex)

            // THEN
            assertEquals(1, results.count { it == SmsProcessResult.Skipped })
        }

    /*
     * @feature Sms parsing
     * @scenario Skip trashed parcel
     * @case Provide SMS with code of trashed parcel - return Skipped
     */
    @Test
    fun process_provideCodeOfTrashedParcel_returnSkipped() =
        runTest {
            // GIVEN
            coEvery { dao.getByCode("905432") } returns provideTrashedParcel("905432")

            val rawList = listOf(rawSms(smsBodyMulti("905432", parcelCount = 2)))

            // WHEN
            val results = processor.process(rawList, defaultCodeRegex, defaultLockerRegex)

            // THEN
            assertEquals(1, results.count { it == SmsProcessResult.Skipped })
        }

    /*
     * @feature Sms parsing
     * @scenario Skip trashed parcel
     * @case Provide SMS with code of trashed parcel - DB not updated
     */
    @Test
    fun process_provideCodeOfTrashedParcel_dbNotUpdated() =
        runTest {
            // GIVEN
            coEvery { dao.getByCode("905432") } returns provideTrashedParcel("905432")

            val rawList = listOf(rawSms(smsBodyMulti("905432", parcelCount = 2)))

            // WHEN
            processor.process(rawList, defaultCodeRegex, defaultLockerRegex)

            // THEN
            coVerify(exactly = 0) { dao.update(any()) }
            coVerify(exactly = 0) { dao.insert(any()) }
        }

    /*
     * @feature Sms parsing
     * @scenario Skip non-locker SMS
     * @case Provide SMS not matching lockerRegex - skipped, not counted
     */
    @Test
    fun process_provideSmsNotMatchingLockerRegex_returnEmptyResults() =
        runTest {
            // GIVEN
            val rawList = listOf(provideNonLockerSms())

            // WHEN
            val results = processor.process(rawList, defaultCodeRegex, defaultLockerRegex)

            // THEN
            assertEquals(0, results.size)
        }

    /*
     * @feature Sms parsing
     * @scenario Skip non-locker SMS
     * @case Provide SMS not matching lockerRegex - DB not touched
     */
    @Test
    fun process_provideSmsNotMatchingLockerRegex_dbNotTouched() =
        runTest {
            // GIVEN
            val rawList = listOf(provideNonLockerSms())

            // WHEN
            processor.process(rawList, defaultCodeRegex, defaultLockerRegex)

            // THEN
            coVerify(exactly = 0) { dao.insert(any()) }
            coVerify(exactly = 0) { dao.update(any()) }
        }

    /*
     * @feature Sms parsing
     * @scenario The parcel locker is full.
     * @case Provide SMS body Locker is Full - return Imported
     */
    @Test
    fun process_provideSmsBodyLockerFull_returnImported() =
        runTest {
            // GIVEN
            coEvery { dao.getByCode(any()) } returns null

            val rawList = listOf(provideBodyLockerFull())

            // WHEN
            val results = processor.process(rawList, defaultCodeRegex, defaultLockerRegex)

            // THEN
            assertEquals(1, results.count { it == SmsProcessResult.Imported })
        }

    /*
     * @feature Sms parsing
     * @scenario The parcel locker is full.
     * @case Provide SMS body Locker is Full - insert parcel with correct code
     */
    @Test
    fun process_provideSmsBodyLockerFull_insertWithCorrectCode() =
        runTest {
            // GIVEN
            coEvery { dao.getByCode(any()) } returns null

            val rawList = listOf(provideBodyLockerFull())

            // WHEN
            processor.process(rawList, defaultCodeRegex, defaultLockerRegex)

            // THEN
            coVerify(exactly = 1) { dao.insert(match { it.code == "937245" }) }
        }

    /*
     * @feature Sms parsing
     * @scenario Locker full then normal SMS arrives, both not in DB
     * @case LockerFull SMS older, normal SMS newer - return 1 Imported
     */
    @Test
    fun process_provideLockerFullThenNormalSms_return1Imported() =
        runTest {
            // GIVEN
            coEvery { dao.getByCode(any()) } returns null

            val rawList =
                listOf(
                    rawSms(smsBodyLockerFull()),
                    rawSms(smsBodyAfterLockerFull()),
                )

            // WHEN
            val results = processor.process(rawList, defaultCodeRegex, defaultLockerRegex)

            // THEN
            assertEquals(1, results.count { it == SmsProcessResult.Imported })
        }

    /*
     * @feature Sms parsing
     * @scenario Locker full then normal SMS arrives, both not in DB
     * @case LockerFull SMS older, normal SMS newer - insert with target location
     */
    @Test
    fun process_provideLockerFullThenNormalSms_insertWithTargetLocation() =
        runTest {
            // GIVEN
            coEvery { dao.getByCode(any()) } returns null

            val rawList =
                listOf(
                    rawSms(smsBodyLockerFull(), date = 1000L),
                    rawSms(smsBodyAfterLockerFull(), date = 2000L),
                )

            // WHEN
            processor.process(rawList, defaultCodeRegex, defaultLockerRegex)

            // THEN
            coVerify(exactly = 1) { dao.insert(match { it.location == "POZ35M Poznan" }) }
        }

    /*
     * @feature Sms parsing
     * @scenario Update existing parcel
     * @case Provide SMS with existing code but changed location - return Updated
     */
    @Test
    fun process_provideExistingCodeWithChangedLocation_returnUpdated() =
        runTest {
            // GIVEN
            coEvery { dao.getByCode("905432") } returns
                provideActiveParcel("905432").copy(
                    location = "WWA35M Warszawa",
                )
            val rawList = listOf(rawSms(smsBodySingleNewLocation("905432")))

            // WHEN
            val results = processor.process(rawList, defaultCodeRegex, defaultLockerRegex)

            // THEN
            assertEquals(1, results.count { it == SmsProcessResult.Updated })
        }

    /*
     * @feature Sms parsing
     * @scenario Update existing parcel
     * @case Provide SMS with existing code but changed location - DB updated with new location
     */
    @Test
    fun process_provideExistingCodeWithChangedLocation_dbUpdatedWithNewLocation() =
        runTest {
            // GIVEN
            coEvery { dao.getByCode("905432") } returns
                provideActiveParcel("905432").copy(
                    location = "WWA35M Warszawa",
                )
            val rawList = listOf(rawSms(smsBodySingleNewLocation("905432")))

            // WHEN
            processor.process(rawList, defaultCodeRegex, defaultLockerRegex)

            // THEN
            coVerify { dao.update(match { it.location == "KRK01M Krakow" }) }
        }

    /*
     * @feature Sms parsing
     * @scenario Deduplicate SMS with same timestamp
     * @case Provide 4 SMS with same code, same timestamp, different parcelCount - highest count chosen
     */
    @Test
    fun process_provide3SmsWithSameCodeAndTimestamp_insertHighestParcelCount() =
        runTest {
            // GIVEN
            coEvery { dao.getByCode(any()) } returns null

            val rawList =
                listOf(
                    rawSms(smsBodySingle("905432"), date = 1000L),
                    rawSms(smsBodyMulti("905432", parcelCount = 2), date = 1000L),
                    rawSms(smsBodyMulti("905432", parcelCount = 4), date = 1000L),
                    rawSms(smsBodyMulti("905432", parcelCount = 3), date = 1000L),
                )

            // WHEN
            processor.process(rawList, defaultCodeRegex, defaultLockerRegex)

            // THEN
            coVerify { dao.insert(match { it.parcelCount == 4 }) }
        }
}
