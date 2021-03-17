package net.ienlab.sogangassist.data

class NotificationItem(
    var id: Int,
    var contentTitle: String,
    var contentText: String,
    var timeStamp: Long,
    var type: Int,
    var isRead: Boolean = false
) {
    companion object {
        val TYPE_REGISTER = 0
        val TYPE_FIREBASE = 1
        val TYPE_HOMEWORK = 2
        val TYPE_LESSON = 3
        val TYPE_SUP_LESSON = 4
        val TYPE_ZOOM = 5
    }
}