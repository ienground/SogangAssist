package net.ienlab.sogangassist.utils

import net.ienlab.sogangassist.adapter.MainWorkAdapter
import net.ienlab.sogangassist.room.LMSEntity

interface ClickCallbackListener {
    fun callBack(position: Int, items: List<LMSEntity>, adapter: MainWorkAdapter)
}