package net.ienlab.sogangassist.data.lms

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LmsDao {
    @Upsert
    suspend fun upsert(data: Lms): Long

    @Delete
    suspend fun delete(data: Lms)

    @Query("SELECT * FROM LMSDatabase WHERE id = :id")
    fun get(id: Long): Flow<Lms>

    @Query("SELECT * FROM LMSDatabase")
    fun getAll(): Flow<List<Lms>>

    @Query("SELECT className FROM LMSDatabase")
    fun getClasses(): Flow<List<String>>

    @Query("SELECT * FROM LMSDatabase WHERE endTime >= :startDate AND endTime < :endDate")
    fun getByEndTime(startDate: Long, endDate: Long): Flow<List<Lms>>

    @Query("SELECT * FROM LMSDatabase WHERE ((type = 0 OR type = 1) AND week = :week AND lesson = :lesson AND className = :className) OR (className = :className AND type = 2 AND homework_name = :homework_name)")
    fun getByData(className: String, week: Int, lesson: Int, homework_name: String): Flow<List<Lms>>

    @Query("SELECT id FROM LMSDatabase WHERE ((type = 0 OR type = 1) AND week = :week AND lesson = :lesson AND className = :className) OR (className = :className AND type = 2 AND homework_name = :homework_name)")
    fun getIdByData(className: String, week: Int, lesson: Int, homework_name: String): Flow<List<Int>>
}