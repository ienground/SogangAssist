package net.ienlab.sogangassist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.data.CardItem
import net.ienlab.sogangassist.databinding.AdapterCardBinding
import net.ienlab.sogangassist.utils.CardAdapter

class CardPagerAdapter(private val context: Context): CardAdapter, PagerAdapter() {
    private lateinit var binding: AdapterCardBinding

    private val views: ArrayList<CardView> = arrayListOf()
    private val datas: ArrayList<CardItem> = arrayListOf()
    private val baseElevation = 0f

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        binding = DataBindingUtil.inflate(inflater, R.layout.adapter_card, container, false)
        binding.tvContent.text = datas[position].getText()

        binding.cardView.maxCardElevation = baseElevation * CardAdapter.MAX_ELEVATION_FACTOR

        views.add(binding.cardView)
        container.addView(binding.root)

        return binding.root
    }

    override fun getBaseElevation(): Float = baseElevation

    override fun getCardViewAt(position: Int): CardView {
        return views[position]
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean = (view == obj)

    override fun getCount(): Int = datas.size

    fun addCardItem(item: CardItem) {
        datas.add(item)
    }

    private fun getRegisteredView(position: Int): CardView = views[position]
}