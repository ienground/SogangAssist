package net.ienlab.sogangassist.data.lms

import android.app.AlarmManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import net.ienlab.sogangassist.Dlog
import net.ienlab.sogangassist.TAG
import net.ienlab.sogangassist.utils.Utils.timeInMillis
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class LmsOfflineRepository(private val dao: LmsDao): LmsRepository {
    override fun getAllStream(): Flow<List<Lms>> = dao.getAll()

    override fun getStream(id: Long): Flow<Lms?> = dao.get(id)

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

    override fun getByDataStream(className: String, week: Int, lesson: Int, homework_name: String): Flow<Lms?> = dao.getByData(className, week, lesson, homework_name)

    override fun getIdByData(className: String, week: Int, lesson: Int, homework_name: String): Flow<Long?> = dao.getIdByData(className, week, lesson, homework_name)

    override suspend fun upsert(data: Lms): Long = dao.upsert(data)

    override suspend fun delete(data: Lms) = dao.delete(data)
}

//class LmsOfflineRepository(private val dao: LmsDao): LmsRepository {
//    val listLesson = listOf(Lms.Type.LESSON, Lms.Type.SUP_LESSON)
//
//    val list = arrayListOf(
//        Lms("Fundamentals of Programming and Problem", System.currentTimeMillis(), Lms.Type.LESSON, LocalDateTime.now().minusWeeks(3).timeInMillis(), LocalDateTime.now().minusWeeks(2).timeInMillis(), true, false, 14, 1, "hi").apply { id = 0 },
//        Lms("Fundamentals of Programming and Problem", System.currentTimeMillis(), Lms.Type.LESSON, LocalDateTime.now().minusWeeks(2).timeInMillis(), LocalDateTime.now().minusWeeks(1).timeInMillis(), true, false, 15, 1, "hi").apply { id = 1 },
//        Lms("Fundamentals of Programming and Problem", System.currentTimeMillis(), Lms.Type.LESSON, LocalDateTime.now().minusWeeks(1).withHour(10).withMinute(30).timeInMillis(), LocalDateTime.now().minusWeeks(0).withHour(16).withMinute(30).timeInMillis(), true, true, 16, 1, "hi").apply { id = 2 },
//        Lms("Fundamentals of Programming and Problem", System.currentTimeMillis(), Lms.Type.LESSON, LocalDateTime.now().minusWeeks(0).withHour(10).withMinute(30).timeInMillis(), LocalDateTime.now().minusWeeks(-1).withHour(16).withMinute(30).timeInMillis(), true, false, 17, 1, "hi").apply { id = 3 },
//        Lms("Creative Algorithms", System.currentTimeMillis(), Lms.Type.HOMEWORK, LocalDateTime.now().minusDays(7).withHour(0).withMinute(0).timeInMillis(), LocalDateTime.now().withHour(23).withMinute(59).timeInMillis(), true, false, 1, 1, "Make your paint apps").apply { id = 4 },
//        Lms("UI/UX Design", System.currentTimeMillis(), Lms.Type.TEAMWORK, LocalDateTime.now().minusDays(7).withHour(0).withMinute(0).timeInMillis(), LocalDateTime.now().withHour(23).withMinute(59).timeInMillis(), true, false, 1, 1, "Team Project 2").apply { id = 5 },
//    ).apply { sortWith(compareBy({ it.isFinished }, { it.endTime })) }
//
//    override fun getAllStream(): Flow<List<Lms>> = flow { emit(list) }
//
//    override fun getStream(id: Long): Flow<Lms?> = flow { emit(list.find { it.id == id }) }
//
//    override fun getClassesStream(): Flow<List<String>> = flow { emit(list.map { it.className }) }
//
//    override fun getByEndTimeStream(date: LocalDate): Flow<List<Lms>> {
//        val endDate = date.plusDays(1)
//        return flow {
//            emit(list.filter { it.endTime in date.timeInMillis() until endDate.timeInMillis() })
//        }
//    }
//
//    override fun getByMonthStream(yearMonth: YearMonth): Flow<List<Lms>> {
//        val startDate = yearMonth.atDay(1).minusWeeks(1)
//        val endDate = yearMonth.atEndOfMonth().plusWeeks(1)
//        return flow {
//            emit(list.filter { it.endTime in startDate.timeInMillis() until endDate.timeInMillis() })
//        }
//    }
//
//    override fun getByDataStream(className: String, week: Int, lesson: Int, homework_name: String): Flow<Lms?> = flow {
//        emit(
//            list.find { it.className == className &&
//                    ((it.type in listLesson && it.week == week && it.lesson == lesson) || (it.type !in listLesson && it.homework_name == homework_name))
//            }
//        )
//    }
//
//    override fun getIdByData(className: String, week: Int, lesson: Int, homework_name: String): Flow<Long?> = flow {
//        emit(
//            list.find {
//                it.className == className &&
//                        ((it.type in listLesson && it.week == week && it.lesson == lesson) || (it.type !in listLesson && it.homework_name == homework_name))
//            }?.id
//        )
//    }
//
//    override suspend fun upsert(data: Lms): Long {
//        list.removeIf { it.id == data.id }
//        list.add(data)
//        list.sortWith(compareBy({ it.isFinished }, { it.endTime }))
//
//        return data.id ?: -1
//    }
//
//    override suspend fun delete(data: Lms) {
//        list.removeIf { it.id == data.id }
//    }
//}