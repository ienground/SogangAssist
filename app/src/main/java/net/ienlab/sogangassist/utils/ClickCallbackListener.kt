package net.ienlab.sogangassist.utils

import net.ienlab.sogangassist.adapter.MainWorkAdapter
import net.ienlab.sogangassist.data.LMSClass

interface ClickCallbackListener {
    fun callBack(position: Int, items: List<LMSClass>, adapter: MainWorkAdapter)
}