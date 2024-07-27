package net.ienlab.sogangassist.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import net.ienlab.sogangassist.MyApplication
import net.ienlab.sogangassist.ui.screen.edit.LmsEditViewModel
import net.ienlab.sogangassist.ui.screen.home.HomeViewModel
import net.ienlab.sogangassist.ui.screen.home.list.LmsListViewModel
import net.ienlab.sogangassist.ui.utils.CalendarMonthItem
import net.ienlab.sogangassist.ui.utils.CalendarMonthItemViewModel

object AppViewModelProvider {
    val factory = viewModelFactory {
        initializer {
            HomeViewModel(
                myApplication(),
                myApplication().container.lmsRepository
            )
        }

        initializer {
            LmsListViewModel(
                myApplication(),
                this.createSavedStateHandle(),
                myApplication().container.lmsRepository
            )
        }

        initializer {
            LmsEditViewModel(
                myApplication(),
                this.createSavedStateHandle(),
                myApplication().container.lmsRepository
            )
        }

        // calendar
        initializer {
            CalendarMonthItemViewModel(
                myApplication(),
                this.createSavedStateHandle(),
                myApplication().container.lmsRepository
            )
        }
    }
}

fun CreationExtras.myApplication(): MyApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)