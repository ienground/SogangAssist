package net.ienlab.sogangassist.data.lms

import kotlinx.coroutines.flow.Flow

class LmsOfflineRepository(private val dao: LmsDao): LmsRepository {
    override fun getAllStream(): Flow<List<Lms>> = dao.getAll()

    override fun getStream(id: Long): Flow<Lms> = dao.get(id)

    override fun getClassesStream(): Flow<List<String>> = dao.getClasses()

    override fun getByEndTimeStream(startDate: Long, endDate: Long): Flow<List<Lms>> = dao.getByEndTime(startDate, endDate)

    override fun getByDataStream(className: String, week: Int, lesson: Int, homework_name: String): Flow<List<Lms>> = dao.getByData(className, week, lesson, homework_name)

    override fun getIdByData(className: String, week: Int, lesson: Int, homework_name: String): Flow<List<Int>> = dao.getIdByData(className, week, lesson, homework_name)

    override suspend fun upsert(data: Lms): Long = dao.upsert(data)

    override suspend fun delete(data: Lms) = dao.delete(data)
}