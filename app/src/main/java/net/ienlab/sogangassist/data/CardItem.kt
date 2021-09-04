package net.ienlab.sogangassist.data

class CardItem(title: String, text: String) {
    private var textResource: String = text
    private var titleResource: String = title

    fun getText(): String = textResource
    fun getTitle(): String = titleResource
}