package io.github.filipiakdawid.skrytkaqr.unit.ui.parcelUiModel

import io.github.filipiakdawid.skrytkaqr.data.model.Parcel
import io.github.filipiakdawid.skrytkaqr.data.model.ParcelStatus

interface ParcelUiModelTrait {
    fun buildParcel(
        expiryDate: String = "",
        expiryTime: String = "",
        smsDate: Long = 1000L,
    ): Parcel =
        Parcel(
            id = 1L,
            code = "905432",
            parcelNumber = "",
            location = "WWA35M Warszawa",
            expiryDate = expiryDate,
            expiryTime = expiryTime,
            smsDate = smsDate,
            status = ParcelStatus.ACTIVE,
            parcelCount = 1,
        )
}
