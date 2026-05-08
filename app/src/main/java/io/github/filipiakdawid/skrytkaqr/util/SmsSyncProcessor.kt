package io.github.filipiakdawid.skrytkaqr.util

import io.github.filipiakdawid.skrytkaqr.data.db.ParcelDao
import io.github.filipiakdawid.skrytkaqr.data.model.Parcel
import io.github.filipiakdawid.skrytkaqr.data.model.ParcelStatus

sealed interface SmsProcessResult {
    data object Skipped : SmsProcessResult

    data object Imported : SmsProcessResult

    data object Updated : SmsProcessResult
}

class SmsSyncProcessor(private val dao: ParcelDao) {
    suspend fun process(
        rawList: List<RawSms>,
        codeRegex: String,
        lockerRegex: String,
    ): List<SmsProcessResult> {
        return rawList
            .mapNotNull { sms ->
                SmsParser.parse(sms.body, codeRegex, lockerRegex)?.let { parsed -> sms to parsed }
            }
            .sortedWith(
                compareByDescending<Pair<RawSms, SmsParseResult>> { it.first.date }
                    .thenByDescending { it.second.parcelCount },
            )
            .distinctBy { (_, parsed) -> parsed.code }
            .map { (sms, parsed) -> processSingleSms(sms, parsed) }
    }

    private suspend fun processSingleSms(
        sms: RawSms,
        parsed: SmsParseResult,
    ): SmsProcessResult {
        val existing = dao.getByCode(parsed.code)

        if (existing == null) {
            dao.insert(
                Parcel(
                    parcelNumber = parsed.parcelNumber,
                    code = parsed.code,
                    location = parsed.location,
                    expiryDate = parsed.expiryDate,
                    expiryTime = parsed.expiryTime,
                    smsDate = sms.date,
                    parcelCount = parsed.parcelCount,
                ),
            )
            return SmsProcessResult.Imported
        }

        if (existing.status != ParcelStatus.ACTIVE) return SmsProcessResult.Skipped

        val needsUpdate =
            existing.location != parsed.location ||
                existing.expiryDate != parsed.expiryDate ||
                existing.expiryTime != parsed.expiryTime ||
                existing.parcelCount != parsed.parcelCount
        if (!needsUpdate) return SmsProcessResult.Skipped

        dao.update(
            existing.copy(
                code = parsed.code,
                location = parsed.location,
                expiryDate = parsed.expiryDate,
                expiryTime = parsed.expiryTime,
                smsDate = sms.date,
                parcelCount = parsed.parcelCount,
            ),
        )
        return SmsProcessResult.Updated
    }
}
