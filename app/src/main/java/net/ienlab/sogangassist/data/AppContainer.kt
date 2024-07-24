package net.ienlab.sogangassist.data

import android.content.Context
import net.ienlab.sogangassist.data.lms.LmsDatabase
import net.ienlab.sogangassist.data.lms.LmsOfflineRepository
import net.ienlab.sogangassist.data.lms.LmsRepository

interface AppContainer {
    val lmsRepository: LmsRepository
}

class AppDataContainer(private val context: Context): AppContainer {
    override val lmsRepository: LmsRepository by lazy {
        LmsOfflineRepository(LmsDatabase.getDatabase(context).getDao())
    }
}
