package net.ienlab.sogangassist

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*
import kotlin.collections.ArrayList

val dbName = "SogangLMSAssistData.db"
val dbVersion = 2

class DBHelper//생성자 - database 파일을 생성한다.
(context: Context, name: String, factory: SQLiteDatabase.CursorFactory?, version: Int) :
        SQLiteOpenHelper(context, name, factory, version) {

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
        sb.append(" $HOMEWORK_NAME TEXT )")

        db.execSQL(sb.toString())
    }

    //DB 업그레이드 필요 시 호출. (version값에 따라 반응)
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $_TABLENAME0")
        onCreate(db)

//        if (oldVersion < 3) {
//            try {
//                db.beginTransaction()
//                db.execSQL("ALTER TABLE $_TABLENAME0 ADD COLUMN ")
//            }
//        }
    }

    fun addItem(item: LMSClass) {
        val db = writableDatabase

        val sb = StringBuffer()
        sb.append(" INSERT INTO $_TABLENAME0 ( ")
        sb.append(" $CLASS_NAME, $TIMESTAMP, $TYPE, $START_TIME, $END_TIME, $LESSON_WEEK, $LESSON_LESSON, $HOMEWORK_NAME ) ")
        sb.append(" VALUES ( ?, ?, ?, ?, ?, ?, ?, ? )")

        db.execSQL(sb.toString(),
                arrayOf(
                    item.className,
                    item.timeStamp,
                    item.type,
                    item.startTime,
                    item.endTime,
                    item.week,
                    item.lesson,
                    item.homework_name
                )
        )

    }

    fun updateItem(item: LMSClass) {
        val db = writableDatabase
        val value = ContentValues()

//        value.put(ID, item.id)
        value.put(CLASS_NAME, item.className)
        value.put(TIMESTAMP, item.timeStamp)
        value.put(TYPE, item.type)
        value.put(START_TIME, item.startTime)
        value.put(END_TIME, item.endTime)
        value.put(LESSON_WEEK, item.week)
        value.put(LESSON_LESSON, item.lesson)
        value.put(HOMEWORK_NAME, item.homework_name)

        db.update(_TABLENAME0, value, "(($TYPE=${LMSType.LESSON} OR $TYPE=${LMSType.SUP_LESSON}) AND $LESSON_WEEK=${item.week} AND $LESSON_LESSON=${item.lesson} AND $CLASS_NAME='${item.className}') OR ($CLASS_NAME='${item.className}' AND $TYPE=${LMSType.HOMEWORK} AND $HOMEWORK_NAME='${item.homework_name}')", null)
    }

    fun getItemAtLastDate(date: Long): List<LMSClass> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val sb = StringBuffer()
        sb.append(" SELECT $ID, $CLASS_NAME, $TIMESTAMP, $TYPE, $START_TIME, $END_TIME, $LESSON_WEEK, $LESSON_LESSON, $HOMEWORK_NAME FROM $_TABLENAME0 WHERE $END_TIME >= ${calendar.timeInMillis} AND $END_TIME < ${calendar.timeInMillis + 24 * 60 * 60 * 1000} ")

        val db = readableDatabase
        val cursor = db.rawQuery(sb.toString(), null)

        val arr = ArrayList<LMSClass>()

        while (cursor.moveToNext()) {
            LMSClass().let {
                it.id = cursor.getInt(0)
                it.className = cursor.getString(1)
                it.timeStamp = cursor.getLong(2)
                it.type = cursor.getInt(3)
                it.startTime = cursor.getLong(4)
                it.endTime = cursor.getLong(5)
                it.week = cursor.getInt(6)
                it.lesson = cursor.getInt(7)
                it.homework_name = cursor.getString(8)

                arr.add(it)
            }
        }

        cursor.close()
        return arr
    }

    fun getAllData(): List<LMSClass> {
        val sb = StringBuffer()
        sb.append(" SELECT $ID, $CLASS_NAME, $TIMESTAMP, $TYPE, $START_TIME, $END_TIME, $LESSON_WEEK, $LESSON_LESSON, $HOMEWORK_NAME FROM $_TABLENAME0 ")

        val db = readableDatabase
        val cursor = db.rawQuery(sb.toString(), null)

        val arr = ArrayList<LMSClass>()

        while (cursor.moveToNext()) {
            LMSClass().let {
                it.id = cursor.getInt(0)
                it.className = cursor.getString(1)
                it.timeStamp = cursor.getLong(2)
                it.type = cursor.getInt(3)
                it.startTime = cursor.getLong(4)
                it.endTime = cursor.getLong(5)
                it.week = cursor.getInt(6)
                it.lesson = cursor.getInt(7)
                it.homework_name = cursor.getString(8)

                arr.add(it)
            }
        }

        cursor.close()
        return arr
    }

    fun CheckIsDataAlreadyInDBorNot(dbfield: String, fieldValue: String): Boolean {
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

}

