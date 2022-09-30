package net.ienlab.sogangassist.callback

import net.ienlab.sogangassist.room.LMSEntity


interface MainLMSClickCallback {
    fun callBack(position: Int, data: LMSEntity)
}