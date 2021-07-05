package net.ienlab.sogangassist.data

class LMSClass (
    var id: Int,
    var className: String,
    var timeStamp: Long,
    var type: Int,
    var startTime: Long, // ONLY HOMEWORK
    var endTime: Long,
    var isRenewAllowed: Boolean,
    var isFinished: Boolean,

    // TYPE = LESSON, SUP_LESSON
    var week: Int,
    var lesson: Int,

    // TYPE = HOMEWORK
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
}