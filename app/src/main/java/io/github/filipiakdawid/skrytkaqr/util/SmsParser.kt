package io.github.filipiakdawid.skrytkaqr.util

data class SmsParseResult(
    val code: String,
    val location: String,
    val expiryDate: String,
    val expiryTime: String,
    val parcelNumber: String,
    val parcelCount: Int = 1,
)

object SmsParser {
    fun parse(
        body: String,
        codeRegex: String,
        lockerRegex: String,
    ): SmsParseResult? {
        val lockerPattern =
            runCatching { Regex(lockerRegex, RegexOption.IGNORE_CASE) }.getOrNull()
                ?: return null
        if (!lockerPattern.containsMatchIn(body)) return null

        val codePattern = runCatching { Regex(codeRegex) }.getOrNull() ?: return null
        val code = codePattern.find(body)?.groupValues?.getOrNull(1) ?: return null

        val location =
            Regex("""w skrytce w ([A-Z0-9]+[^,]+)""")
                .find(body)?.groupValues?.getOrNull(1)?.trim()
                ?: Regex("""w pobliskim Paczkomacie ([A-Z0-9]+[^,]+)""")
                    .find(body)?.groupValues?.getOrNull(1)?.trim()
                ?: Regex("""Paczkomat\s+([A-Z0-9]+[^,]+)""")
                    .find(body)?.groupValues?.getOrNull(1)?.trim()
                ?: ""

        val expiryMatch =
            Regex("""do (\d{2}/\d{2}/\d{4})(?:\s+(\d{2}:\d{2}))?""")
                .find(body)

        val expiryDate =
            expiryMatch?.groupValues?.getOrNull(1)
                ?: Regex("""do (\d{4})-(\d{2})-(\d{2})""")
                    .find(body)?.destructured
                    ?.let { (y, m, d) -> "$d/$m/$y" } // yyyy-MM-dd → dd/MM/yyyy
                ?: ""
        val expiryTime = expiryMatch?.groupValues?.getOrNull(2) ?: ""

        val parcelNumber =
            Regex("""\b(\d{18,24})\b""")
                .find(body)?.groupValues?.getOrNull(1) ?: ""

        val parcelCount =
            Regex("""Liczba paczek:?\s*(\d+)""", RegexOption.IGNORE_CASE)
                .find(body)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1

        return SmsParseResult(
            code = code,
            location = location,
            expiryDate = expiryDate,
            expiryTime = expiryTime,
            parcelNumber = parcelNumber,
            parcelCount = parcelCount,
        )
    }

    fun testCodeRegex(
        regex: String,
        sample: String,
    ): String? {
        val pattern = runCatching { Regex(regex) }.getOrNull() ?: return null
        return pattern.find(sample)?.groupValues?.getOrNull(1)
    }

    fun testLockerRegex(
        regex: String,
        sample: String,
    ): Boolean {
        val pattern = runCatching { Regex(regex, RegexOption.IGNORE_CASE) }.getOrNull() ?: return false
        return pattern.containsMatchIn(sample)
    }

    fun isValidRegex(pattern: String): Boolean =
        runCatching {
            Regex(pattern)
            true
        }.getOrDefault(false)
}
