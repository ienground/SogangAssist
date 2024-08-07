package net.ienlab.sogangassist.ui.screen.home

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.ienlab.sogangassist.Dlog
import net.ienlab.sogangassist.MyApplication
import net.ienlab.sogangassist.TAG
import net.ienlab.sogangassist.data.lms.LmsDatabase
import net.ienlab.sogangassist.data.lms.LmsRepository
import net.ienlab.sogangassist.ui.screen.edit.LmsDetails
import net.ienlab.sogangassist.ui.screen.edit.toLmsDetails
import net.ienlab.sogangassist.ui.screen.home.list.LmsListUiState
import net.ienlab.sogangassist.ui.screen.home.list.LmsListViewModel
import net.ienlab.sogangassist.ui.screen.home.list.LmsListViewModel.Companion
import net.ienlab.sogangassist.utils.Utils.timeInMillis
import java.time.LocalDate
import java.time.YearMonth

class HomeViewModel(
    myApplication: MyApplication,
    private val lmsRepository: LmsRepository
): AndroidViewModel(myApplication) {
    private val context: Context
        get() = getApplication<MyApplication>().applicationContext
    var uiState by mutableStateOf(HomeUiState())
        private set

    private var _uiStateList = MutableStateFlow(HomeUiStateList())
    var uiStateList = _uiStateList.asStateFlow()
    private var job: Job? = null

    fun updateUiState(details: HomeDetails) {
        uiState = HomeUiState(item = details)
    }

    fun updateUiStateList() {
        job?.cancel()
        job = viewModelScope.launch {
            lmsRepository.getByMonthStream(uiState.item.currentMonth).collect {
                _uiStateList.value = HomeUiStateList(it.map { lms -> lms.toLmsDetails() })
            }
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    init {
        updateUiStateList()
    }
}

data class HomeUiStateList(
    val lmsList: List<LmsDetails> = listOf(),
    val isInitialized: Boolean = false
)

data class HomeUiState(
    val item: HomeDetails = HomeDetails(),
)

data class HomeDetails(
    val isCalendarExpand: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.now(),
)