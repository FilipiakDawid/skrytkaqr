package io.github.filipiakdawid.skrytkaqr.util

import android.content.Context
import android.provider.Telephony

data class RawSms(val address: String, val body: String, val date: Long)

object SmsReader {
    fun readNewSms(
        context: Context,
        senderFilter: String,
        afterTimestamp: Long,
    ): List<RawSms> {
        val results = mutableListOf<RawSms>()

        val projection =
            arrayOf(
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
            )
        val selection = "date > ? AND address LIKE ?"
        val selectionArgs = arrayOf(afterTimestamp.toString(), "%$senderFilter%")

        context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "date DESC",
        )?.use { cursor ->
            val addrIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            while (cursor.moveToNext()) {
                results.add(
                    RawSms(
                        address = cursor.getString(addrIdx),
                        body = cursor.getString(bodyIdx),
                        date = cursor.getLong(dateIdx),
                    ),
                )
            }
        }
        return results
    }
}
