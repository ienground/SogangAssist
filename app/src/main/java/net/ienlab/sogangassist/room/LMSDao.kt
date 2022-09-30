package net.ienlab.sogangassist.room

import androidx.room.*

@Dao
interface LMSDao {
    @Query("SELECT * FROM LMSDatabase")
    fun getAll(): List<LMSEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(data: LMSEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(data: LMSEntity): Int

    @Query("SELECT * FROM LMSDatabase WHERE id = :id")
    fun get(id: Long): LMSEntity

    @Query("SELECT className FROM LMSDatabase")
    fun getClasses(): List<String>

    @Query("SELECT * FROM LMSDatabase WHERE endTime >= :startDate AND endTime < :endDate")
    fun getByEndTime(startDate: Long, endDate: Long): List<LMSEntity>

    @Query("SELECT * FROM LMSDatabase WHERE ((type = 0 OR type = 1) AND week = :week AND lesson = :lesson AND className = :className) OR (className = :className AND type = 2 AND homework_name = :homework_name)")
    fun getByData(className: String, week: Int, lesson: Int, homework_name: String): List<LMSEntity>

    @Query("SELECT id FROM LMSDatabase WHERE ((type = 0 OR type = 1) AND week = :week AND lesson = :lesson AND className = :className) OR (className = :className AND type = 2 AND homework_name = :homework_name)")
    fun getIdByData(className: String, week: Int, lesson: Int, homework_name: String): List<Int>

    @Query("DELETE FROM LMSDatabase WHERE id = :id")
    fun delete(id: Long)

    @Query("SELECT EXISTS(SELECT * FROM LMSDatabase WHERE ((type = 0 OR type = 1) AND week = :week AND lesson = :lesson AND className = :className) OR (className = :className AND type = 2 AND homework_name = :homework_name) )")
    fun checkIsAlreadyInDB(className: String, week: Int, lesson: Int, homework_name: String): Boolean
}