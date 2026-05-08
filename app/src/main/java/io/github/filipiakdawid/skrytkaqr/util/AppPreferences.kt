package io.github.filipiakdawid.skrytkaqr.util

import android.content.Context
import androidx.core.content.edit

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("skrytkaqr_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_PHONE_NUMBER = ""
        const val DEFAULT_SENDER_FILTER = "InPost"
        const val DEFAULT_CODE_REGEX = """Kod odbioru:?\s*(\d{6})"""
        const val DEFAULT_LOCKER_REGEX = """skrytce|skrytka|paczkomacie"""
        const val DEFAULT_SMS_SAMPLE = "Paczka czeka w skrytce w WWA35M Warszawa, Warszawa 515A do 06/06/2015 19:34. Kod odbioru 000000."
        const val TRASH_TTL_DAYS = 7L
    }

    var installTimestamp: Long
        get() = prefs.getLong("install_timestamp", 0L)
        set(v) = prefs.edit { putLong("install_timestamp", v) }

    var phoneNumber: String
        get() = prefs.getString("phone_number", DEFAULT_PHONE_NUMBER) ?: DEFAULT_PHONE_NUMBER
        set(v) = prefs.edit { putString("phone_number", v) }

    var senderFilter: String
        get() = prefs.getString("sender_filter", DEFAULT_SENDER_FILTER) ?: DEFAULT_SENDER_FILTER
        set(v) = prefs.edit { putString("sender_filter", v) }

    var codeRegex: String
        get() = prefs.getString("code_regex", DEFAULT_CODE_REGEX) ?: DEFAULT_CODE_REGEX
        set(v) = prefs.edit { putString("code_regex", v) }

    var lockerRegex: String
        get() = prefs.getString("locker_regex", DEFAULT_LOCKER_REGEX) ?: DEFAULT_LOCKER_REGEX
        set(v) = prefs.edit { putString("locker_regex", v) }

    var smsSample: String
        get() = prefs.getString("sms_sample", DEFAULT_SMS_SAMPLE) ?: DEFAULT_SMS_SAMPLE
        set(v) = prefs.edit { putString("sms_sample", v) }

    var lastSyncTimestamp: Long
        get() = prefs.getLong("last_sync_timestamp", 0L)
        set(v) = prefs.edit { putLong("last_sync_timestamp", v) }

    fun resetAdvancedToDefaults() {
        prefs.edit {
            putString("code_regex", DEFAULT_CODE_REGEX)
            putString("locker_regex", DEFAULT_LOCKER_REGEX)
            putString("sms_sample", DEFAULT_SMS_SAMPLE)
        }
    }
}
