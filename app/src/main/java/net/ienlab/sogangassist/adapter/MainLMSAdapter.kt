package net.ienlab.sogangassist.adapter

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.activity.EditActivity2
import net.ienlab.sogangassist.callback.MainAllListClickCallbackListener
import net.ienlab.sogangassist.callback.MainLMSClickCallback
import net.ienlab.sogangassist.constant.IntentKey
import net.ienlab.sogangassist.room.LMSEntity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

class MainLMSAdapter(var items: ArrayList<LMSEntity>, var calendar: Calendar): RecyclerView.Adapter<MainLMSAdapter.ItemViewHolder>() {

    lateinit var context: Context

    private var clickCallbackListener: MainAllListClickCallbackListener? = null
    private var timeFormat = SimpleDateFormat("a h:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        context = parent.context
        return ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_main_lms, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.ivIcon.setImageResource(when (items[holder.adapterPosition].type) {
            LMSEntity.TYPE_LESSON -> R.drawable.ic_video
            LMSEntity.TYPE_SUP_LESSON -> R.drawable.ic_video_sup
            LMSEntity.TYPE_HOMEWORK -> R.drawable.ic_assignment
            LMSEntity.TYPE_ZOOM -> R.drawable.ic_video
            LMSEntity.TYPE_TEAMWORK -> R.drawable.ic_video
            LMSEntity.TYPE_EXAM -> R.drawable.ic_video
            else -> R.drawable.ic_video
        })
        holder.tvEndtime.text = timeFormat.format(Date(items[holder.adapterPosition].endTime))
        holder.tvTitle.text = when (items[holder.adapterPosition].type) {
            LMSEntity.TYPE_LESSON, LMSEntity.TYPE_SUP_LESSON -> context.getString(R.string.week_lesson_format, items[holder.adapterPosition].week, items[holder.adapterPosition].lesson)
            else -> items[holder.adapterPosition].homework_name
        }
        holder.tvClassName.text = items[holder.adapterPosition].className

        val hourLeft = TimeUnit.HOURS.convert(items[holder.adapterPosition].endTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS).toInt()
        val minuteLeft = TimeUnit.MINUTES.convert(items[holder.adapterPosition].endTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS).toInt() % 60

        holder.tvLefttime.text = if (hourLeft != 0 && minuteLeft != 0) context.getString(R.string.time_left_hour_min, hourLeft, minuteLeft)
            else if (hourLeft != 0) context.getString(R.string.time_left_hour, hourLeft)
            else context.getString(R.string.time_left_min, minuteLeft)
        holder.icCheck.visibility = if (items[holder.adapterPosition].isFinished) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener {
            clickCallbackListener?.callBack(holder.adapterPosition, items[holder.adapterPosition])
        }
        if (items[holder.adapterPosition].endTime - System.currentTimeMillis() < 0) {
            holder.tvLefttime.text = context.getString(R.string.finished_item)
            holder.tvEndtime.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.gray))
        }
    }

    fun setCallbackListener(listener: MainAllListClickCallbackListener) {
        this.clickCallbackListener = listener
    }

    inner class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
        val tvEndtime: TextView = itemView.findViewById(R.id.tv_endtime)
        val tvLefttime: TextView = itemView.findViewById(R.id.tv_lefttime)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvClassName: TextView = itemView.findViewById(R.id.tv_class_name)
        val icCheck: ImageView = itemView.findViewById(R.id.ic_check)
    }
}