package net.ienlab.sogangassist.data.lms

import android.app.AlarmManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.ienlab.sogangassist.Dlog
import net.ienlab.sogangassist.TAG
import net.ienlab.sogangassist.utils.Utils.timeInMillis
import java.time.LocalDate
import java.time.YearMonth

class LmsOfflineRepository(private val dao: LmsDao): LmsRepository {
    override fun getAllStream(): Flow<List<Lms>> = dao.getAll()

    override fun getStream(id: Long): Flow<Lms> = dao.get(id)

    override fun getClassesStream(): Flow<List<String>> = dao.getClasses()

    override fun getByEndTimeStream(date: LocalDate): Flow<List<Lms>> {
        val endDate = date.plusDays(1)
        return dao.getByEndTime(date.timeInMillis(), endDate.timeInMillis())
    }

    override fun getByMonthStream(yearMonth: YearMonth): Flow<List<Lms>> {
        val startDate = yearMonth.atDay(1).minusWeeks(1)
        val endDate = yearMonth.atEndOfMonth().plusWeeks(1)
        return dao.getByEndTime(startDate.timeInMillis(), endDate.timeInMillis())
    }

    override fun getByDataStream(className: String, week: Int, lesson: Int, homework_name: String): Flow<Lms> = dao.getByData(className, week, lesson, homework_name)

    override fun getIdByData(className: String, week: Int, lesson: Int, homework_name: String): Flow<Int> = dao.getIdByData(className, week, lesson, homework_name)

    override suspend fun upsert(data: Lms): Long = dao.upsert(data)

    override suspend fun delete(data: Lms) = dao.delete(data)
}