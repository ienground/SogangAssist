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

    @Query("DELETE FROM LMSDatabase WHERE id = :id")
    fun delete(id: Long)

    @Query("SELECT EXISTS(SELECT * FROM LMSDatabase WHERE id = :id)")
    fun checkIsAlreadyInDB(id: Long): Boolean
}