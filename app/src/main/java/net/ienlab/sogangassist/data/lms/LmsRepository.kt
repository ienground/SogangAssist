package net.ienlab.sogangassist.data.lms

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface LmsRepository {
    fun getAllStream(): Flow<List<Lms>>

    fun getStream(id: Long): Flow<Lms>

    fun getClassesStream(): Flow<List<String>>

    fun getByEndTimeStream(date: LocalDate): Flow<List<Lms>>

    fun getByDataStream(className: String, week: Int, lesson: Int, homework_name: String): Flow<List<Lms>>

    fun getIdByData(className: String, week: Int, lesson: Int, homework_name: String): Flow<List<Int>>

    suspend fun upsert(data: Lms): Long

    suspend fun delete(data: Lms)
}