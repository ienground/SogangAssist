package net.ienlab.sogangassist.ui.screen.edit

import android.app.AlarmManager
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.ienlab.sogangassist.MyApplication
import net.ienlab.sogangassist.data.lms.Lms
import net.ienlab.sogangassist.data.lms.LmsRepository
import net.ienlab.sogangassist.utils.Utils.deleteLmsSchedule
import net.ienlab.sogangassist.utils.Utils.parseLongToLocalDateTime
import net.ienlab.sogangassist.utils.Utils.setLmsSchedule
import net.ienlab.sogangassist.utils.Utils.timeInMillis
import net.ienlab.sogangassist.utils.Utils.toSafeInt
import net.ienlab.sogangassist.utils.Utils.toSafeString
import java.time.LocalDate
import java.time.LocalDateTime

class LmsEditViewModel(
    application: MyApplication,
    savedStateHandle: SavedStateHandle,
    private val lmsRepository: LmsRepository
): AndroidViewModel(application) {
    private val context: Context
        get() = getApplication<MyApplication>().applicationContext
    private val am: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val itemId: Long = checkNotNull(savedStateHandle[LmsEditDestination.itemIdArg])

    var classNames = listOf<String>()
        private set
    var uiState by mutableStateOf(LmsUiState())
        private set

    init {
        viewModelScope.launch {
            classNames = lmsRepository.getClassesStream().first()
            uiState =
                if (itemId != -1L) {
                    lmsRepository.getStream(itemId)
                        .filterNotNull()
                        .first().let {
                            LmsUiState(it.toLmsDetails(), isInitialized = true)
                        }
                } else {
                    LmsUiState(isInitialized = true)
                }
        }
    }

    fun updateUiState(item: LmsDetails) {
        uiState = LmsUiState(item = item, isInitialized = uiState.isInitialized)
    }

    suspend fun saveItem(): Boolean {
        return if (validateInput()) {
            val id = lmsRepository.upsert(uiState.item.toLms())
            setLmsSchedule(context, am, uiState.item.toLms().apply { this.id = id })
            true
        } else false
    }

    suspend fun deleteItem() {
        deleteLmsSchedule(context, am, uiState.item.toLms())
        lmsRepository.delete(uiState.item.toLms())
    }

    private fun validateInput(item: LmsDetails = uiState.item): Boolean {
        val listLesson = listOf(Lms.Type.LESSON, Lms.Type.SUP_LESSON)
        return if (item.className.isEmpty()) false
            else if (item.type in listLesson && (item.week.isEmpty() || item.lesson.isEmpty())) false
            else if (item.type !in listLesson && item.homeworkName.isEmpty()) false
            else if (!item.startTime.isBefore(item.endTime)) false
            else true
     }

}

data class LmsUiState(
    val item: LmsDetails = LmsDetails(),
    val isInitialized: Boolean = false,
)

data class LmsDetails(
    val id: Long = -1,
    var className: String = "",
    var timestamp: LocalDateTime = LocalDateTime.now(),
    var type: Int = Lms.Type.LESSON,
    var startTime: LocalDateTime = LocalDateTime.now(),
    var endTime: LocalDateTime = LocalDateTime.now().plusDays(1),
    var isRenewAllowed: Boolean = true,
    var isFinished: Boolean = false,
    var week: String = "",
    var lesson: String = "",
    var homeworkName: String = "",

    val dropdownExpanded: Boolean = false,
    val tempDate: LocalDate = LocalDate.MIN,
    val showStartDatePicker: Boolean = false,
    val showStartTimePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,
    val showEndTimePicker: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showSaveDialog: Boolean = false
)

fun LmsDetails.toLms(): Lms = Lms(className, timestamp.timeInMillis(), type, startTime.timeInMillis(), endTime.timeInMillis(), isRenewAllowed, isFinished, week.toSafeInt(), lesson.toSafeInt(), homeworkName)
    .apply { id = this@toLms.id.let { if (it == -1L) null else it } }

fun Lms.toLmsDetails(): LmsDetails = LmsDetails(id ?: -1, className, parseLongToLocalDateTime(timestamp), type, parseLongToLocalDateTime(startTime), parseLongToLocalDateTime(endTime), isRenewAllowed, isFinished, week.toSafeString(), lesson.toSafeString(), homework_name)