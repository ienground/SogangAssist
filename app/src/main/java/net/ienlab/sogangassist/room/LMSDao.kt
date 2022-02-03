package net.ienlab.sogangassist.room

import androidx.room.*

@Dao
interface LMSDao {
    @Query("SELECT * FROM LMS_ASSIST")
    fun getAll(): List<LMSEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(data: LMSEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(data: LMSEntity): Int

    @Query("SELECT * FROM LMS_ASSIST WHERE id = :id")
    fun get(id: Long): LMSEntity

    @Query("DELETE FROM LMS_ASSIST WHERE id = :id")
    fun delete(id: Long)

    @Query("SELECT EXISTS(SELECT * FROM LMS_ASSIST WHERE id = :id)")
    fun checkIsAlreadyInDB(id: Long): Boolean
}