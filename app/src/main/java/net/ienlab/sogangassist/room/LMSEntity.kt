package net.ienlab.sogangassist.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "LMSDatabase")
class LMSEntity(
    var className: String,
    var timestamp: Long,
    var type: Int,
    var startTime: Long, // ONLY HOMEWORK, TEAMWORK
    var endTime: Long,
    var isRenewAllowed: Boolean,
    var isFinished: Boolean,

    // TYPE = LESSON, SUP_LESSON
    var week: Int,
    var lesson: Int,

    // TYPE = HOMEWORK, TEAMWORK
    var homework_name: String
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

    fun same(obj: LMSEntity): Boolean {
        if (className != obj.className) return false
        if (type != obj.type) return false
        if ((type == TYPE_HOMEWORK || type == TYPE_TEAMWORK) && startTime != obj.startTime) return false
        if (endTime != obj.endTime) return false
        if (isRenewAllowed != obj.isRenewAllowed) return false
        if (isFinished != obj.isFinished) return false
        if ((type == TYPE_LESSON || type == TYPE_SUP_LESSON) && week != obj.week) return false
        if ((type == TYPE_LESSON || type == TYPE_SUP_LESSON) && lesson != obj.lesson) return false
        if ((type == TYPE_HOMEWORK || type == TYPE_TEAMWORK) && homework_name != obj.homework_name) return false

        return true
    }

}