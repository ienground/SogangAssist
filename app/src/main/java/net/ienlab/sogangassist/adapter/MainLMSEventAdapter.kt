package net.ienlab.sogangassist.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textview.MaterialTextView
import de.hdodenhof.circleimageview.CircleImageView
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.activity.TAG
import net.ienlab.sogangassist.callback.MainLMSClickCallback
import net.ienlab.sogangassist.room.LMSEntity
import net.ienlab.sogangassist.utils.ClickCallbackListener
import net.ienlab.sogangassist.utils.MyUtils.Companion.timeZero
import net.ienlab.sogangassist.utils.MyUtils.Companion.tomorrowZero
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MainLMSEventAdapter(var items: ArrayList<LMSEntity>, var calendar: Calendar): RecyclerView.Adapter<MainLMSEventAdapter.ItemViewHolder>() {

    lateinit var context: Context
    private var callbackListener: ClickCallbackListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.adapter_main_lms_event, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val apmFormat = SimpleDateFormat("a", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm", Locale.getDefault())
        val apmTimeFormat = SimpleDateFormat(context.getString(R.string.apmTimeFormat), Locale.getDefault())
        val time = Calendar.getInstance().apply {
            timeInMillis = items[holder.absoluteAdapterPosition].endTime
        }
        val colorOnSecondaryContainer = TypedValue().apply { context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSecondaryContainer, this, true) }
        val colorOutline = TypedValue().apply { context.theme.resolveAttribute(com.google.android.material.R.attr.colorOutline, this, true) }

        holder.ivIcon.setImageResource(when (items[holder.absoluteAdapterPosition].type) {
            LMSEntity.TYPE_LESSON -> R.drawable.ic_video
            LMSEntity.TYPE_SUP_LESSON -> R.drawable.ic_video_sup
            LMSEntity.TYPE_HOMEWORK -> R.drawable.ic_assignment
            LMSEntity.TYPE_ZOOM -> R.drawable.ic_live_class
            LMSEntity.TYPE_TEAMWORK -> R.drawable.ic_team
            LMSEntity.TYPE_EXAM -> R.drawable.ic_test
            else -> R.drawable.ic_video
        })

        holder.tvApm.visibility = if (Locale.getDefault() == Locale.KOREA) View.GONE else View.VISIBLE
        holder.tvApmKo.visibility = if (Locale.getDefault() != Locale.KOREA) View.GONE else View.VISIBLE

        holder.tvHomeworkName.text = when (items[holder.absoluteAdapterPosition].type) {
            LMSEntity.TYPE_LESSON, LMSEntity.TYPE_SUP_LESSON -> context.getString(R.string.week_lesson_format, items[holder.absoluteAdapterPosition].week, items[holder.absoluteAdapterPosition].lesson)
            else -> items[holder.absoluteAdapterPosition].homework_name
        }
        items[holder.absoluteAdapterPosition].className.let {
            if (it != "") {
                holder.tvClass.visibility = View.VISIBLE
                holder.tvClass.text = it
            } else {
                holder.tvClass.visibility = View.GONE
            }
        }

        holder.ivCheck.visibility = if (items[holder.absoluteAdapterPosition].isFinished) View.VISIBLE else View.GONE
        holder.tvApm.text = apmFormat.format(time.time)
        holder.tvApmKo.text = apmFormat.format(time.time)
        holder.tvTime.text = timeFormat.format(time.time)

        val dayLeft = TimeUnit.DAYS.convert(items[holder.absoluteAdapterPosition].endTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS).toInt()
        val hourLeft = TimeUnit.HOURS.convert(items[holder.absoluteAdapterPosition].endTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS).toInt()
        val minuteLeft = TimeUnit.MINUTES.convert(items[holder.absoluteAdapterPosition].endTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS).toInt() % 60

        holder.tvLeftTime.text = if (items[holder.absoluteAdapterPosition].endTime - System.currentTimeMillis() > 0) {
            if (dayLeft != 0) context.getString(R.string.time_left_day, dayLeft)
            else if (hourLeft != 0 && minuteLeft != 0) context.getString(R.string.time_left_hour_min, hourLeft, minuteLeft)
            else if (hourLeft != 0) context.getString(R.string.time_left_hour, hourLeft)
            else context.getString(R.string.time_left_min, minuteLeft)
        } else {
            if (dayLeft != 0) context.getString(R.string.time_past_day, -dayLeft)
            else if (hourLeft != 0 && minuteLeft != 0) context.getString(R.string.time_past_hour_min, -hourLeft, -minuteLeft)
            else if (hourLeft != 0) context.getString(R.string.time_past_hour, -hourLeft)
            else context.getString(R.string.time_past_min, -minuteLeft)
        }

        val colorTvLeftTime = TypedValue().apply { context.theme.resolveAttribute(
            if (items[holder.absoluteAdapterPosition].endTime - System.currentTimeMillis() > 0) com.google.android.material.R.attr.colorOnSecondaryContainer
            else com.google.android.material.R.attr.colorError,
            this, true) }
        val colorTextTvLeftTime = TypedValue().apply { context.theme.resolveAttribute(
            if (items[holder.absoluteAdapterPosition].endTime - System.currentTimeMillis() > 0) com.google.android.material.R.attr.colorSecondaryContainer
            else com.google.android.material.R.attr.colorOnError,
            this, true) }
        holder.tvLeftTime.backgroundTintList = ColorStateList.valueOf(colorTvLeftTime.data)
        holder.tvLeftTime.setTextColor(ColorStateList.valueOf(colorTextTvLeftTime.data))

        holder.tvApm.typeface = ResourcesCompat.getFont(context, if (!items[holder.absoluteAdapterPosition].isFinished) R.font.pretendard_black else R.font.pretendard)
        holder.tvApmKo.typeface = ResourcesCompat.getFont(context, if (!items[holder.absoluteAdapterPosition].isFinished) R.font.pretendard_black else R.font.pretendard)
        holder.tvTime.typeface = ResourcesCompat.getFont(context, if (!items[holder.absoluteAdapterPosition].isFinished) R.font.pretendard_black else R.font.pretendard)
        holder.tvApm.setTextColor(if (!items[holder.absoluteAdapterPosition].isFinished) colorOnSecondaryContainer.data else colorOutline.data)
        holder.tvApmKo.setTextColor(if (!items[holder.absoluteAdapterPosition].isFinished) colorOnSecondaryContainer.data else colorOutline.data)
        holder.tvTime.setTextColor(if (!items[holder.absoluteAdapterPosition].isFinished) colorOnSecondaryContainer.data else colorOutline.data)
        holder.tvHomeworkName.setTextColor(if (!items[holder.absoluteAdapterPosition].isFinished) colorOnSecondaryContainer.data else colorOutline.data)
        holder.tvClass.setTextColor(if (!items[holder.absoluteAdapterPosition].isFinished) colorOnSecondaryContainer.data else colorOutline.data)

        holder.itemView.setOnClickListener {
            callbackListener?.callBack(holder.absoluteAdapterPosition, items[holder.absoluteAdapterPosition])
        }

        holder.itemView.setOnLongClickListener {
            callbackListener?.longClick(holder.absoluteAdapterPosition, items[holder.absoluteAdapterPosition])
            true
        }

        holder.btnDelete.setOnClickListener {
            callbackListener?.delete(holder.absoluteAdapterPosition, items[holder.absoluteAdapterPosition])
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            for (payload in payloads) {
                if (payload is String) {
                    when (payload) {
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun edit(id: Long, item: LMSEntity) {
        val position = items.indexOfFirst { it.id == id }
        if (position != -1) {
            if (item.endTime in calendar.timeZero().timeInMillis until calendar.tomorrowZero().timeInMillis) {
                items[position] = item
                notifyItemChanged(position)
            } else {
                items.removeAt(position)
                notifyItemRemoved(position)
            }
        } else {
            if (item.endTime in calendar.timeZero().timeInMillis until calendar.tomorrowZero().timeInMillis) {
                items.add(item)
                items.sortBy { it.endTime }
                val newPosition = items.indexOf(item)
                notifyItemInserted(newPosition)
            }
        }
    }

    fun delete(id: Long) {
        val position = items.indexOfFirst { it.id == id }
        if (position != -1) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun setClickCallback(callbackListener: ClickCallbackListener) {
        this.callbackListener = callbackListener
    }

    inner class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvHomeworkName: MaterialTextView = itemView.findViewById(R.id.tv_homework_name)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btn_delete)
        val tvClass: MaterialTextView = itemView.findViewById(R.id.tv_class)
        val tvApm: MaterialTextView = itemView.findViewById(R.id.tv_apm)
        val tvApmKo: MaterialTextView = itemView.findViewById(R.id.tv_apm_ko)
        val tvTime: MaterialTextView = itemView.findViewById(R.id.tv_time)
        val ivColor: CircleImageView = itemView.findViewById(R.id.iv_color)
        val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
        val ivCheck: ImageView = itemView.findViewById(R.id.iv_check)
        val tvLeftTime: MaterialTextView = itemView.findViewById(R.id.tv_left_time)
    }

    companion object {
        const val PAYLOAD_TOGGLE = "payload_toggle"
    }
}