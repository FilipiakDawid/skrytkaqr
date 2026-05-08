package io.github.filipiakdawid.skrytkaqr.util

import android.content.Context
import android.provider.Telephony
import androidx.core.net.toUri

object MmsReader {
    // 137 = PduHeaders.FROM
    private const val ADDRESS_TYPE_FROM = "137"

    fun readNewMms(
        context: Context,
        senderFilter: String,
        afterTimestamp: Long,
    ): List<RawSms> {
        val afterTimestampSeconds = afterTimestamp / 1000
        val results = mutableListOf<RawSms>()

        val projection = arrayOf(Telephony.Mms._ID, Telephony.Mms.DATE)
        val selection = "${Telephony.Mms.DATE} > ?"
        val selectionArgs = arrayOf(afterTimestampSeconds.toString())

        context.contentResolver.query(
            Telephony.Mms.Inbox.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${Telephony.Mms.DATE} DESC",
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(Telephony.Mms._ID)
            val dateIdx = cursor.getColumnIndexOrThrow(Telephony.Mms.DATE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIdx)
                val dateMs = cursor.getLong(dateIdx) * 1000L

                val address = getSenderAddress(context, id) ?: continue
                if (!address.contains(senderFilter, ignoreCase = true)) continue

                val body = getTextBody(context, id) ?: continue
                results.add(RawSms(address = address, body = body, date = dateMs))
            }
        }
        return results
    }

    private fun getSenderAddress(
        context: Context,
        mmsId: Long,
    ): String? {
        val uri = "content://mms/$mmsId/addr".toUri()
        context.contentResolver.query(
            uri,
            arrayOf("address", "type"),
            "type = $ADDRESS_TYPE_FROM",
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val address = cursor.getString(0)
                if (!address.isNullOrBlank() && address != "insert-address-token") {
                    return address
                }
            }
        }
        return null
    }

    private fun getTextBody(
        context: Context,
        mmsId: Long,
    ): String? {
        val uri = "content://mms/$mmsId/part".toUri()
        val body = StringBuilder()

        context.contentResolver.query(
            uri,
            arrayOf("ct", "text"),
            "ct = ?",
            arrayOf("text/plain"),
            null,
        )?.use { cursor ->
            val textIdx = cursor.getColumnIndexOrThrow("text")
            while (cursor.moveToNext()) {
                cursor.getString(textIdx)?.let { body.append(it) }
            }
        }

        return body.toString().trim().takeIf { it.isNotBlank() }
    }
}
