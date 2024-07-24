package net.ienlab.sogangassist.constant

object Intents {
    object Id {
        const val MARKING_RESULT = "marking_result"
    }

    object Key {
        const val ITEM_ID = "ID"
        const val NOTI_ID = "NOTI_ID"
        const val NOTI_TYPE = "noti_type"
        const val TRIGGER = "TRIGGER"
        const val TIME = "TIME"
        const val MINUTE = "MINUTE"
        const val ACTION_TYPE = "action_type" // Int
    }

    object Value {
        const val ACTION_EDIT = 0
        const val ACTION_DELETE = 1
        const val ACTION_EDIT_START = 2

        object NotiType {
            const val FIREBASE_PUSH = 1
        }
    }
}