package net.ienlab.sogangassist.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [LMSEntity::class], version = 3)
abstract class LMSDatabase: RoomDatabase() {
    abstract fun getDao(): LMSDao

    companion object {
        private var instance: LMSDatabase? = null
        fun getInstance(context: Context): LMSDatabase? {
            if (instance == null) {
                val migration2to3 = object: Migration(2, 3) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL("""
                            CREATE TABLE LMSDatabase (
                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                className TEXT NOT NULL,
                                timestamp INTEGER NOT NULL,
                                type INTEGER NOT NULL,
                                startTime INTEGER NOT NULL,
                                endTime INTEGER NOT NULL,
                                isRenewAllowed INTEGER NOT NULL,
                                isFinished INTEGER NOT NULL,
                                week INTEGER NOT NULL,
                                lesson INTEGER NOT NULL,
                                homework_name TEXT NOT NULL
                            )""".trimIndent())

                        database.execSQL("""
                            INSERT INTO LMSDatabase (id, className, timestamp, type, startTime, endTime, isRenewAllowed, isFinished, week, lesson, homework_name)
                            SELECT ID, CLASS_NAME, TIMESTAMP, TYPE, START_TIME, END_TIME, ALLOW_RENEW, IS_FINISHED, LESSON_WEEK, LESSON_LESSON, HOMEWORK_NAME From LMS_ASSIST
                        """.trimIndent())

                        database.execSQL("DROP TABLE LMS_ASSIST")
                    }
                }

                synchronized(LMSDatabase::class) {
                    instance = Room.databaseBuilder(context.applicationContext, LMSDatabase::class.java, "SogangLMSAssistData.db")
                        .addMigrations(migration2to3)
                        .build()
                }
            }

            return instance
        }
    }
}