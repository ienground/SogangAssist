package net.ienlab.sogangassist.callback

import net.ienlab.sogangassist.data.lms.Lms


interface MainLMSClickCallback {
    fun callBack(position: Int, data: Lms)
}