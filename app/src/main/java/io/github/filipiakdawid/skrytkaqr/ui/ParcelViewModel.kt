package io.github.filipiakdawid.skrytkaqr.ui

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.filipiakdawid.skrytkaqr.data.db.AppDatabase
import io.github.filipiakdawid.skrytkaqr.data.model.Parcel
import io.github.filipiakdawid.skrytkaqr.data.model.ParcelStatus
import io.github.filipiakdawid.skrytkaqr.util.AppPreferences
import io.github.filipiakdawid.skrytkaqr.util.MmsReader
import io.github.filipiakdawid.skrytkaqr.util.QrGenerator
import io.github.filipiakdawid.skrytkaqr.util.SmsProcessResult
import io.github.filipiakdawid.skrytkaqr.util.SmsReader
import io.github.filipiakdawid.skrytkaqr.util.SmsSyncProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class SyncResult(val imported: Int, val updated: Int)

enum class ParcelAction { MOVED_TO_TRASH, RESTORED, DELETED }

class ParcelViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = AppPreferences(app)
    private val db = AppDatabase.getInstance(app)
    private val dao = db.parcelDao()
    private val syncProcessor = SmsSyncProcessor(dao)
    private val _activeParcels = MutableStateFlow<List<Parcel>>(emptyList())
    val activeParcels: StateFlow<List<Parcel>> = _activeParcels
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing
    private val _trashParcels = MutableStateFlow<List<Parcel>>(emptyList())
    val trashParcels: StateFlow<List<Parcel>> = _trashParcels

    val activeParcelsUi: StateFlow<List<ParcelUiModel>> =
        _activeParcels
            .map { list -> list.map { it.toUiModel() } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val trashParcelsUi: StateFlow<List<ParcelUiModel>> =
        _trashParcels
            .map { list -> list.map { it.toUiModel() } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _lastSyncResult = MutableStateFlow<SyncResult?>(null)
    val lastSyncResult: StateFlow<SyncResult?> = _lastSyncResult

    private val _lastAction = MutableStateFlow<ParcelAction?>(null)
    val lastAction: StateFlow<ParcelAction?> = _lastAction

    private val _phoneNumber = MutableStateFlow(prefs.phoneNumber)
    private val _senderFilter = MutableStateFlow(prefs.senderFilter)
    private val _codeRegex = MutableStateFlow(prefs.codeRegex)
    private val _lockerRegex = MutableStateFlow(prefs.lockerRegex)
    private val _smsSample = MutableStateFlow(prefs.smsSample)

    val phoneNumber: StateFlow<String> = _phoneNumber
    val senderFilter: StateFlow<String> = _senderFilter
    val codeRegex: StateFlow<String> = _codeRegex
    val lockerRegex: StateFlow<String> = _lockerRegex
    val smsSample: StateFlow<String> = _smsSample

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (prefs.installTimestamp == 0L) {
                prefs.installTimestamp = System.currentTimeMillis()
            }
            purgeOldTrash()
            launch { dao.getByStatus(ParcelStatus.ACTIVE).collect { _activeParcels.value = it } }
            launch { dao.getByStatus(ParcelStatus.TRASH).collect { _trashParcels.value = it } }
        }
        syncSms()
    }

    fun clearSyncResult() {
        _lastSyncResult.value = null
    }

    fun clearLastAction() {
        _lastAction.value = null
    }

    fun hasSmsPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            getApplication(),
            android.Manifest.permission.READ_SMS,
        ) == PackageManager.PERMISSION_GRANTED

    fun hasPhoneNumber(): Boolean = prefs.phoneNumber.isNotBlank()

    fun setPhoneNumber(value: String) {
        prefs.phoneNumber = value
        _phoneNumber.value = value
    }

    fun setSenderFilter(value: String) {
        prefs.senderFilter = value
        _senderFilter.value = value
    }

    fun setCodeRegex(value: String) {
        prefs.codeRegex = value
        _codeRegex.value = value
    }

    fun setLockerRegex(value: String) {
        prefs.lockerRegex = value
        _lockerRegex.value = value
    }

    fun setSmsSample(value: String) {
        prefs.smsSample = value
        _smsSample.value = value
    }

    fun resetAdvancedToDefaults() {
        prefs.resetAdvancedToDefaults()
        _codeRegex.value = AppPreferences.DEFAULT_CODE_REGEX
        _lockerRegex.value = AppPreferences.DEFAULT_LOCKER_REGEX
        _smsSample.value = AppPreferences.DEFAULT_SMS_SAMPLE
    }

    fun syncSms() {
        if (!hasSmsPermission()) return
        if (_isSyncing.value) return
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            try {
                val lastTimestamp = prefs.lastSyncTimestamp.takeIf { it > 0L } ?: prefs.installTimestamp

                val smsList =
                    SmsReader.readNewSms(
                        context = getApplication(),
                        senderFilter = prefs.senderFilter,
                        afterTimestamp = lastTimestamp,
                    )
                val mmsList =
                    MmsReader.readNewMms(
                        context = getApplication(),
                        senderFilter = prefs.senderFilter,
                        afterTimestamp = lastTimestamp,
                    )

                val rawList = (smsList + mmsList).sortedByDescending { it.date }

                val results = syncProcessor.process(rawList, prefs.codeRegex, prefs.lockerRegex)

                if (rawList.isNotEmpty()) {
                    prefs.lastSyncTimestamp = rawList.maxOf { it.date }
                }

                _lastSyncResult.value =
                    SyncResult(
                        imported = results.count { it == SmsProcessResult.Imported },
                        updated = results.count { it == SmsProcessResult.Updated },
                    )
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun moveToTrash(parcel: Parcel) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.update(parcel.copy(status = ParcelStatus.TRASH, trashedAt = System.currentTimeMillis()))
            _lastAction.value = ParcelAction.MOVED_TO_TRASH
        }
    }

    fun restore(parcel: Parcel) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.update(parcel.copy(status = ParcelStatus.ACTIVE, trashedAt = null))
            _lastAction.value = ParcelAction.RESTORED
        }
    }

    fun deleteFromTrash(parcel: Parcel) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(parcel)
            _lastAction.value = ParcelAction.DELETED
        }
    }

    fun generateQrBitmap(
        code: String,
        size: Int = 512,
    ) = QrGenerator.generate(QrGenerator.buildQrContent(prefs.phoneNumber, code), size)

    private fun purgeOldTrash() {
        viewModelScope.launch(Dispatchers.IO) {
            val ttl = TimeUnit.DAYS.toMillis(AppPreferences.TRASH_TTL_DAYS)
            dao.purgeOldTrash(System.currentTimeMillis() - ttl)
        }
    }
}
