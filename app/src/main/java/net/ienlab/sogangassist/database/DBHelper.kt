package net.ienlab.sogangassist.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import net.ienlab.sogangassist.data.LMSClass
import java.util.*
import kotlin.collections.ArrayList

class DBHelper(context: Context, name: String, version: Int): SQLiteOpenHelper(context, name, null, version) {

    val _TABLENAME0 = "LMS_ASSIST"
    val _CREATE0 = "CREATE TABLE IF NOT EXISTS $_TABLENAME0("

    val ID = "ID"
    val CLASS_NAME = "CLASS_NAME"
    val TIMESTAMP = "TIMESTAMP"
    val TYPE = "TYPE"
    val START_TIME = "START_TIME"
    val END_TIME = "END_TIME"
    val LESSON_WEEK = "LESSON_WEEK"
    val LESSON_LESSON = "LESSON_LESSON"
    val HOMEWORK_NAME = "HOMEWORK_NAME"
    val ALLOW_RENEW = "ALLOW_RENEW"
    val IS_FINISHED = "IS_FINISHED"

    //DB 처음 만들때 호출. - 테이블 생성 등의 초기 처리.
    override fun onCreate(db: SQLiteDatabase) {
        val sb = StringBuffer()
        sb.append(" CREATE TABLE $_TABLENAME0 ( ")
        sb.append(" $ID INTEGER PRIMARY KEY AUTOINCREMENT, ")
        sb.append(" $CLASS_NAME TEXT, ")
        sb.append(" $TIMESTAMP INTEGER, ")
        sb.append(" $TYPE INTEGER, ")
        sb.append(" $START_TIME INTEGER, ")
        sb.append(" $END_TIME INTEGER, ")
        sb.append(" $LESSON_WEEK INTEGER, ")
        sb.append(" $LESSON_LESSON INTEGER, ")
        sb.append(" $HOMEWORK_NAME TEXT, ")
        sb.append(" $ALLOW_RENEW INTEGER, ")
        sb.append(" $IS_FINISHED INTEGER )")

        db.execSQL(sb.toString())
    }

    //DB 업그레이드 필요 시 호출. (version값에 따라 반응)
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $_TABLENAME0")
        onCreate(db)
    }

    fun addItem(item: LMSClass): Int {
        val db = writableDatabase

        val sb = StringBuffer()
        sb.append(" INSERT INTO $_TABLENAME0 ( ")
        sb.append(" $CLASS_NAME, $TIMESTAMP, $TYPE, $START_TIME, $END_TIME, $LESSON_WEEK, $LESSON_LESSON, $HOMEWORK_NAME, $ALLOW_RENEW, $IS_FINISHED ) ")
        sb.append(" VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )")

        db.execSQL(sb.toString(),
                arrayOf(
                    item.className,
                    item.timeStamp,
                    item.type,
                    item.startTime,
                    item.endTime,
                    item.week,
                    item.lesson,
                    item.homework_name,
                    if (item.isRenewAllowed) 1 else 0,
                    if (item.isFinished) 1 else 0
                )
        )

        val cursor = db.rawQuery("SELECT LAST_INSERT_ROWID()", null)
        var lastIndex = -1
        while (cursor.moveToNext()) { lastIndex = cursor.getInt(0) }

        cursor.close()
        return lastIndex
    }

    fun updateItem(item: LMSClass) {
        val db = writableDatabase
        val value = ContentValues()

        value.put(CLASS_NAME, item.className)
        value.put(TIMESTAMP, item.timeStamp)
        value.put(TYPE, item.type)
        value.put(START_TIME, item.startTime)
        value.put(END_TIME, item.endTime)
        value.put(LESSON_WEEK, item.week)
        value.put(LESSON_LESSON, item.lesson)
        value.put(HOMEWORK_NAME, item.homework_name)
        value.put(ALLOW_RENEW, item.isRenewAllowed)
        value.put(IS_FINISHED, item.isFinished)

        db.update(_TABLENAME0, value, "(($TYPE=${LMSClass.TYPE_LESSON} OR $TYPE=${LMSClass.TYPE_SUP_LESSON}) AND $LESSON_WEEK=${item.week} AND $LESSON_LESSON=${item.lesson} AND $CLASS_NAME='${item.className}') OR ($CLASS_NAME='${item.className}' AND $TYPE=${LMSClass.TYPE_HOMEWORK} AND $HOMEWORK_NAME='${item.homework_name}')", null)
    }

    fun updateItemById(item: LMSClass) {
        val db = writableDatabase
        val value = ContentValues()

        value.put(ID, item.id)
        value.put(CLASS_NAME, item.className)
        value.put(TIMESTAMP, item.timeStamp)
        value.put(TYPE, item.type)
        value.put(START_TIME, item.startTime)
        value.put(END_TIME, item.endTime)
        value.put(LESSON_WEEK, item.week)
        value.put(LESSON_LESSON, item.lesson)
        value.put(HOMEWORK_NAME, item.homework_name)
        value.put(ALLOW_RENEW, item.isRenewAllowed)
        value.put(IS_FINISHED, item.isFinished)

        db.update(_TABLENAME0, value, "$ID=${item.id}", null)
    }

    fun getItemAtLastDate(date: Long): List<LMSClass> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val sb = StringBuffer()
        sb.append(" SELECT $ID, $CLASS_NAME, $TIMESTAMP, $TYPE, $START_TIME, $END_TIME, $LESSON_WEEK, $LESSON_LESSON, $HOMEWORK_NAME, $ALLOW_RENEW, $IS_FINISHED FROM $_TABLENAME0 WHERE $END_TIME >= ${calendar.timeInMillis} AND $END_TIME < ${calendar.timeInMillis + 24 * 60 * 60 * 1000} ")

        val db = readableDatabase
        val cursor = db.rawQuery(sb.toString(), null)

        val arr = ArrayList<LMSClass>()

        while (cursor.moveToNext()) {
            val data = LMSClass(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getLong(2),
                cursor.getInt(3),
                cursor.getLong(4),
                cursor.getLong(5),
                cursor.getInt(9) == 1,
                cursor.getInt(10) == 1,
                cursor.getInt(6),
                cursor.getInt(7),
                cursor.getString(8)
            )

            arr.add(data)
        }

        cursor.close()
        return arr
    }

    fun getItemMonth(month: Long): List<LMSClass> {
        val startCalendar = Calendar.getInstance().apply {
            timeInMillis = month
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCalendar = Calendar.getInstance().apply {
            timeInMillis = month
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        return getItemDateRange(startCalendar.timeInMillis, endCalendar.timeInMillis)
    }

    fun getItemDateRange(startDate: Long, endDate: Long): List<LMSClass> {
        val startCalendar = Calendar.getInstance().apply {
            timeInMillis = startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCalendar = Calendar.getInstance().apply {
            timeInMillis = endDate
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val sb = StringBuffer()
        sb.append(" SELECT $ID, $CLASS_NAME, $TIMESTAMP, $TYPE, $START_TIME, $END_TIME, $LESSON_WEEK, $LESSON_LESSON, $HOMEWORK_NAME, $ALLOW_RENEW, $IS_FINISHED FROM $_TABLENAME0 WHERE $END_TIME >= ${startCalendar.timeInMillis} AND $END_TIME < ${endCalendar.timeInMillis + 24 * 60 * 60 * 1000} ")

        val db = readableDatabase
        val cursor = db.rawQuery(sb.toString(), null)

        val arr = ArrayList<LMSClass>()

        while (cursor.moveToNext()) {
            val data = LMSClass(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getLong(2),
                cursor.getInt(3),
                cursor.getLong(4),
                cursor.getLong(5),
                cursor.getInt(9) == 1,
                cursor.getInt(10) == 1,
                cursor.getInt(6),
                cursor.getInt(7),
                cursor.getString(8)
            )

            arr.add(data)
        }

        cursor.close()
        return arr
    }

    fun getItemById(id: Int): LMSClass {
        val sb = StringBuffer()
        sb.append(" SELECT $ID, $CLASS_NAME, $TIMESTAMP, $TYPE, $START_TIME, $END_TIME, $LESSON_WEEK, $LESSON_LESSON, $HOMEWORK_NAME, $ALLOW_RENEW, $IS_FINISHED FROM $_TABLENAME0 WHERE $ID=$id ")

        val db = readableDatabase
        val cursor = db.rawQuery(sb.toString(), null)

        var data = LMSClass(-1, "", 0L, 0, 0L, 0L, false, false, -1, -1, "")
        while (cursor.moveToNext()) {
            data = LMSClass(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getLong(2),
                cursor.getInt(3),
                cursor.getLong(4),
                cursor.getLong(5),
                cursor.getInt(9) == 1,
                cursor.getInt(10) == 1,
                cursor.getInt(6),
                cursor.getInt(7),
                cursor.getString(8)
            )
        }

        cursor.close()
        return data
    }

    fun getIdByCondition(item: LMSClass): Int {
        val sb = StringBuffer()
        sb.append(" SELECT $ID FROM $_TABLENAME0 WHERE (($TYPE=${LMSClass.TYPE_LESSON} OR $TYPE=${LMSClass.TYPE_SUP_LESSON}) AND $LESSON_WEEK=${item.week} AND $LESSON_LESSON=${item.lesson} AND $CLASS_NAME='${item.className}') OR ($CLASS_NAME='${item.className}' AND $TYPE=${LMSClass.TYPE_HOMEWORK} AND $HOMEWORK_NAME='${item.homework_name}') ")

        val db = readableDatabase
        val cursor = db.rawQuery(sb.toString(), null)

        var id = -1
        while (cursor.moveToNext()) {
            id = cursor.getInt(0)
        }

        cursor.close()
        return id
    }

    fun checkItemByData(item: LMSClass): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $_TABLENAME0 $_TABLENAME0 WHERE (($TYPE=${LMSClass.TYPE_LESSON} OR $TYPE=${LMSClass.TYPE_SUP_LESSON}) AND $LESSON_WEEK=${item.week} AND $LESSON_LESSON=${item.lesson} AND $CLASS_NAME='${item.className}') OR ($CLASS_NAME='${item.className}' AND $TYPE=${LMSClass.TYPE_HOMEWORK} AND $HOMEWORK_NAME='${item.homework_name}') "
        val cursor = db.rawQuery(query, null)
        if (cursor.count <= 0) {
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }

    fun getAllData(): List<LMSClass> {
        val sb = StringBuffer()
        sb.append(" SELECT $ID, $CLASS_NAME, $TIMESTAMP, $TYPE, $START_TIME, $END_TIME, $LESSON_WEEK, $LESSON_LESSON, $HOMEWORK_NAME, $ALLOW_RENEW, $IS_FINISHED FROM $_TABLENAME0 ")

        val db = readableDatabase
        val cursor = db.rawQuery(sb.toString(), null)

        val arr = ArrayList<LMSClass>()

        while (cursor.moveToNext()) {
            val data = LMSClass(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getLong(2),
                cursor.getInt(3),
                cursor.getLong(4),
                cursor.getLong(5),
                cursor.getInt(9) == 1,
                cursor.getInt(10) == 1,
                cursor.getInt(6),
                cursor.getInt(7),
                cursor.getString(8)
            )

            arr.add(data)
        }

        cursor.close()
        return arr
    }

    fun getAllEndTime(): List<Long> {
        val sb = StringBuffer()
        sb.append(" SELECT $END_TIME FROM $_TABLENAME0 ")

        val db = readableDatabase
        val cursor = db.rawQuery(sb.toString(), null)

        val result = ArrayList<Long>()

        while (cursor.moveToNext()) {
            result.add(cursor.getLong(5))
        }

        cursor.close()
        return result
    }

    fun deleteData(id: Int) {
        val db = writableDatabase
        db.execSQL(" DELETE FROM $_TABLENAME0 WHERE $ID = $id")
    }

    fun checkIsDataAlreadyInDBorNot(dbfield: String, fieldValue: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $_TABLENAME0 WHERE $dbfield = $fieldValue"
        val cursor = db.rawQuery(query, null)
        if (cursor.count <= 0) {
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }

    companion object {
        val dbName = "SogangLMSAssistData.db"
        val dbVersion = 2
    }

}

