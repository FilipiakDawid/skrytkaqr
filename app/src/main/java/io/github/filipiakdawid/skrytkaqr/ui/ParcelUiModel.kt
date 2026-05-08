package io.github.filipiakdawid.skrytkaqr.ui

import io.github.filipiakdawid.skrytkaqr.data.model.Parcel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class ExpiryState { NORMAL, WARNING, EXPIRED }

data class ParcelUiModel(
    val parcel: Parcel,
    val expiryState: ExpiryState,
)

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")

fun Parcel.toUiModel(now: LocalDateTime = LocalDateTime.now()): ParcelUiModel =
    ParcelUiModel(
        parcel = this,
        expiryState = resolveExpiryState(expiryDate, expiryTime, now),
    )

private fun resolveExpiryState(
    expiryDate: String,
    expiryTime: String,
    now: LocalDateTime,
): ExpiryState {
    if (expiryDate.isEmpty()) return ExpiryState.NORMAL
    return runCatching {
        val date = LocalDate.parse(expiryDate, DATE_FORMAT)

        if (expiryTime.isEmpty()) {
            return@runCatching if (date.isBefore(now.toLocalDate())) {
                ExpiryState.EXPIRED
            } else {
                ExpiryState.NORMAL
            }
        }

        val expiry = LocalDateTime.of(date, LocalTime.parse(expiryTime, TIME_FORMAT))
        val hoursLeft = ChronoUnit.HOURS.between(now, expiry)
        when {
            hoursLeft <= 0 -> ExpiryState.EXPIRED
            hoursLeft < 8 -> ExpiryState.WARNING
            else -> ExpiryState.NORMAL
        }
    }.getOrDefault(ExpiryState.NORMAL)
}
