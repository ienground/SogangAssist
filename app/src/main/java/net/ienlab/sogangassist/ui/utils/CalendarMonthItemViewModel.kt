package net.ienlab.sogangassist.ui.utils

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import net.ienlab.sogangassist.MyApplication
import net.ienlab.sogangassist.data.lms.LmsRepository

class CalendarMonthItemViewModel(
    application: MyApplication,
    savedStateHandle: SavedStateHandle,
    private val lmsRepository: LmsRepository
): ViewModel() {
}