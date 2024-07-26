package net.ienlab.sogangassist.ui.screen.home.list

import android.app.AlarmManager
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.type.DateTime
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import net.ienlab.sogangassist.Dlog
import net.ienlab.sogangassist.MyApplication
import net.ienlab.sogangassist.TAG
import net.ienlab.sogangassist.data.lms.LmsRepository
import net.ienlab.sogangassist.ui.screen.edit.LmsDetails
import net.ienlab.sogangassist.ui.screen.edit.toLms
import net.ienlab.sogangassist.ui.screen.edit.toLmsDetails
import net.ienlab.sogangassist.utils.Utils.deleteLmsSchedule
import net.ienlab.sogangassist.utils.Utils.parseLongToLocalDate
import net.ienlab.sogangassist.utils.Utils.parseLongToLocalDateTime
import net.ienlab.sogangassist.utils.Utils.setLmsSchedule
import net.ienlab.sogangassist.utils.Utils.timeInMillis
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar

class LmsListViewModel(
    application: MyApplication,
    savedStateHandle: SavedStateHandle,
    private val lmsRepository: LmsRepository
): AndroidViewModel(application) {
    private val context: Context
        get() = getApplication<MyApplication>().applicationContext
    private val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val _uiStateList = Channel<LmsListUiState>()
    var uiStateList = _uiStateList.receiveAsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = LmsListUiState()
        )
    val date: LocalDate = savedStateHandle.get<Long?>(LmsListDestination.timeArg)?.let { parseLongToLocalDate(it) } ?: LocalDate.now()
    val todayStateList = lmsRepository.getByEndTimeStream(date).map { LmsListUiState(it.map { lms ->
        lms.toLmsDetails()
    }, isInitialized = true) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = LmsListUiState()
        )

    init {
        uiStateList =
            lmsRepository.getByEndTimeStream(date).map { LmsListUiState(it.map { lms ->
                lms.toLmsDetails()
            }, isInitialized = true) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                    initialValue = LmsListUiState()
                )
    }

    suspend fun saveItem(item: LmsDetails) {
        val id = lmsRepository.upsert(item.toLms())
        if (!item.isFinished) {
            setLmsSchedule(context, am, item.toLms().apply { this.id = id })
        }
    }

    suspend fun deleteItem(item: LmsDetails) {
        deleteLmsSchedule(context, am, item.toLms())
        lmsRepository.delete(item.toLms())
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class LmsListUiState(
    val lmsList: List<LmsDetails> = listOf(),
    val isInitialized: Boolean = false
)

