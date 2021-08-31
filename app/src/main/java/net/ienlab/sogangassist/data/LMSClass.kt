package net.ienlab.sogangassist.data

class LMSClass (
    var id: Int,
    var className: String,
    var timeStamp: Long,
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

    fun same(obj: LMSClass): Boolean {
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