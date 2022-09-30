package net.ienlab.sogangassist.callback

import net.ienlab.sogangassist.room.LMSEntity


interface MainAllListClickCallbackListener {
    fun callBack(position: Int, data: LMSEntity)
}