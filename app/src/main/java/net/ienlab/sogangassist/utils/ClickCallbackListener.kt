package net.ienlab.sogangassist.utils

import net.ienlab.sogangassist.adapter.MainWorkAdapter
import net.ienlab.sogangassist.room.LMSEntity

interface ClickCallbackListener {
    fun callBack(position: Int, entity: LMSEntity)
    fun longClick(position: Int, entity: LMSEntity)
    fun delete(position: Int, entity: LMSEntity)
}