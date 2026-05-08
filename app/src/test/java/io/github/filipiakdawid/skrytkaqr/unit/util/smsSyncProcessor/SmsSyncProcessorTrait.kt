package io.github.filipiakdawid.skrytkaqr.unit.util.smsSyncProcessor

import io.github.filipiakdawid.skrytkaqr.data.model.Parcel
import io.github.filipiakdawid.skrytkaqr.data.model.ParcelStatus
import io.github.filipiakdawid.skrytkaqr.util.RawSms

interface SmsSyncProcessorTrait {
    fun smsBodySingle(code: String): String =
        "Paczka czeka w skrytce w WWA35M Warszawa, Warszawa 515A do 16/04/2026 09:31. Kod odbioru $code."

    fun smsBodyMulti(
        code: String,
        parcelCount: Int,
    ): String =
        "W Twojej skrytce jest wiecej paczek. Liczba paczek $parcelCount. " +
            "Paczkomat WWA01M Warszawa, Warszawa 515A czeka na odbior. " +
            "Kod odbioru $code dla wszystkich paczek. Czas odbioru do 02/05/2026 09:02."

    fun smsBodyLockerFull(): String =
        """
    Twoj Paczkomat jest przepelniony. Paczka 632625396060813017651337 jest tymczasowo magazynowana w pobliskim Paczkomacie POZ82M Sniadeckich 40, skad mozesz ja odebrac do 2020-11-06 wlacznie.
    Po tym terminie dostarczymy ja do pierwotnie wybranego Paczkomatu.
    Kod odbioru 937245.
    """

    fun smsBodySingleNewLocation(code: String): String =
        "Paczka czeka w skrytce w KRK01M Krakow, Krakow 1A do 16/04/2026 09:31. Kod odbioru $code."

    fun smsBodyAfterLockerFull(): String = "Paczka czeka w skrytce w POZ35M Poznan, Poznan 1A do 16/04/2026 09:31. Kod odbioru 937245."

    fun rawSms(
        body: String,
        date: Long = 1000L,
    ): RawSms = RawSms(address = "InPost", body = body, date = date)

    fun provideSingleNewSms(
        code: String = "905432",
        date: Long = 1000L,
    ): RawSms = rawSms(smsBodySingle(code), date = date)

    fun provideNonLockerSms(
        code: String = "905432",
        date: Long = 1000L,
    ): RawSms = rawSms("Twoja paczka jest w drodze. Kod odbioru $code.", date = date)

    fun provideBodyLockerFull(): RawSms = rawSms(smsBodyLockerFull())

    fun provideActiveParcel(
        code: String,
        parcelCount: Int = 1,
        location: String = "WWA35M Warszawa",
        expiryDate: String = "16/04/2026",
        expiryTime: String = "09:31",
    ): Parcel =
        Parcel(
            id = 1L,
            code = code,
            parcelNumber = "",
            location = location,
            expiryDate = expiryDate,
            expiryTime = expiryTime,
            smsDate = 1000L,
            status = ParcelStatus.ACTIVE,
            parcelCount = parcelCount,
        )

    fun provideTrashedParcel(code: String): Parcel =
        Parcel(
            id = 1L,
            code = code,
            parcelNumber = "",
            location = "WWA35M Warszawa",
            expiryDate = "16/04/2026",
            expiryTime = "09:31",
            smsDate = 1000L,
            status = ParcelStatus.TRASH,
            trashedAt = System.currentTimeMillis(),
            parcelCount = 1,
        )
}
