package net.ienlab.sogangassist.ui.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.YearMonth

class HomeViewModel: ViewModel() {
    var uiState by mutableStateOf(HomeUiState())
        private set

    fun updateUiState(details: HomeDetails) {
        uiState = HomeUiState(item = details)
    }
}

data class HomeUiState(
    val item: HomeDetails = HomeDetails()
)

data class HomeDetails(
    val isCalendarExpand: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.now(),
)