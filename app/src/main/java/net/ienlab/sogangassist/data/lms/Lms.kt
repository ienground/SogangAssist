package net.ienlab.sogangassist.data.lms

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "LMSDatabase")
data class Lms(
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
    @PrimaryKey(autoGenerate = true) var id: Long? = null

    override fun toString(): String = "[$id] $className / ${Date(timestamp)} / $type / ${Date(startTime)} / ${Date(endTime)} / $isRenewAllowed / $isFinished / ${week}-${lesson} / $homework_name"

    object Type {
        const val LESSON = 0
        const val SUP_LESSON = 1
        const val HOMEWORK = 2
        const val ZOOM = 3
        const val TEAMWORK = 4
        const val EXAM = 5
    }
}