package net.ienlab.sogangassist.constant

object FirebasePushKey {
    const val TITLE = "title_eng"
    const val TITLE_KOR = "title_kor"
    const val MESSAGE = "message_eng"
    const val MESSAGE_KOR = "message_kor"
    const val TYPE = "type"
    const val IMAGE = "image"

    object Types {
        const val NONE = -1
        const val LOGO = 0
        const val VERSION_UP = 2
        const val WARNING = 3
    }
}