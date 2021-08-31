package net.ienlab.sogangassist.utils

import net.ienlab.sogangassist.adapter.NotificationsAdapter
import net.ienlab.sogangassist.data.NotificationItem

interface NotiClickCallbackListener {
    fun callBack(position: Int, items: List<NotificationItem>, adapter: NotificationsAdapter)
}