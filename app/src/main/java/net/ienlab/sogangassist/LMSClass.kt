package net.ienlab.sogangassist

class LMSClass {
    var id: Int = 0
    lateinit var className: String
    var timeStamp: Long = 0
    var type: Int = 0
    var startTime: Long = 0 // ONLY HOMEWORK
    var endTime: Long = 0
    var isRenewAllowed = true
    var isFinished = false

    // TYPE = LESSON, SUP_LESSON
    var week: Int = 0
    var lesson: Int = 0

    // TYPE = HOMEWORK
    lateinit var homework_name: String
}