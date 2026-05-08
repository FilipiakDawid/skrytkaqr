package io.github.filipiakdawid.skrytkaqr.unit.util.smsParser

interface SmsParserTrait {
    fun smsStandard(): String = "Paczka czeka w skrytce w WWA35M Warszawa, Warszawa 515A do 16/04/2026 09:31. Kod odbioru 905432."

    fun smsMultiLocker(): String =
        "Uwaga! W Twojej skrytce jest wiecej paczek. Liczba paczek 2. " +
            "Paczkomat WWA01M Warszawa, Warszawa 515A czeka na odbior. " +
            "Kod odbioru 133713 dla wszystkich paczek. " +
            "Czas odbioru do 02/05/2026 09:02. Nieodebrane paczki wroca do Nadawcow."

    fun smsWithParcelNumber(): String =
        "Paczka 123456789012345678 czeka w skrytce w WWA35M Warszawa, Warszawa 515A do 16/04/2026. Kod odbioru 905432."

    fun smsNoDate(): String = "Paczka czeka w skrytce w WWA35M Warszawa. Kod odbioru 905432."

    fun smsLockerFull(): String =
        """
    Twoj Paczkomat jest przepelniony. Paczka 632625396060813017651337 jest tymczasowo magazynowana w pobliskim Paczkomacie POZ82M Sniadeckich 40, skad mozesz ja odebrac do 2020-11-06 wlacznie.
    Po tym terminie dostarczymy ja do pierwotnie wybranego Paczkomatu.
    Kod odbioru 937245.
    """
}
