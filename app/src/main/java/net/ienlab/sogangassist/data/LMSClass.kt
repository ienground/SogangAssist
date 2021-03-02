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
        val LESSON = 0
        val SUP_LESSON = 1
        val HOMEWORK = 2
        val ZOOM = 3
    }
}