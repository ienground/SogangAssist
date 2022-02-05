package net.ienlab.sogangassist.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.ienlab.sogangassist.data.LMSClass
import java.util.*

@Entity(tableName = "LMSDatabase")
class LMSEntity(
    val className: String,
    val timestamp: Long,
    val type: Int,
    val startTime: Long, // ONLY HOMEWORK, TEAMWORK
    val endTime: Long,
    val isRenewAllowed: Boolean,
    val isFinished: Boolean,

    // TYPE = LESSON, SUP_LESSON
    val week: Int,
    val lesson: Int,

    // TYPE = HOMEWORK, TEAMWORK
    val homework_name: String
) {
    companion object {
        const val TYPE_LESSON = 0
        const val TYPE_SUP_LESSON = 1
        const val TYPE_HOMEWORK = 2
        const val TYPE_ZOOM = 3
        const val TYPE_TEAMWORK = 4
        const val TYPE_EXAM = 5
    }

    @PrimaryKey(autoGenerate = true) var id: Int? = null

    fun setDataId(id: Int) {
        this.id = id
    }

    override fun toString(): String = "[$id] $className / ${Date(timestamp)} / $type / ${Date(startTime)} / ${Date(endTime)} / $isRenewAllowed / $isFinished / ${week}-${lesson} / $homework_name"

    fun same(obj: LMSClass): Boolean {
        if (className != obj.className) return false
        if (type != obj.type) return false
        if ((type == LMSClass.TYPE_HOMEWORK || type == LMSClass.TYPE_TEAMWORK) && startTime != obj.startTime) return false
        if (endTime != obj.endTime) return false
        if (isRenewAllowed != obj.isRenewAllowed) return false
        if (isFinished != obj.isFinished) return false
        if ((type == LMSClass.TYPE_LESSON || type == LMSClass.TYPE_SUP_LESSON) && week != obj.week) return false
        if ((type == LMSClass.TYPE_LESSON || type == LMSClass.TYPE_SUP_LESSON) && lesson != obj.lesson) return false
        if ((type == LMSClass.TYPE_HOMEWORK || type == LMSClass.TYPE_TEAMWORK) && homework_name != obj.homework_name) return false

        return true
    }

}