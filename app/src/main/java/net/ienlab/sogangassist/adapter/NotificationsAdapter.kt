package net.ienlab.sogangassist.adapter

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import net.ienlab.sogangassist.activity.*
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.data.NotificationItem
import net.ienlab.sogangassist.database.NotiDBHelper
import net.ienlab.sogangassist.utils.ItemActionListener
import net.ienlab.sogangassist.utils.MyBottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationsAdapter(private var items: ArrayList<NotificationItem>) : RecyclerView.Adapter<NotificationsAdapter.ItemViewHolder>(), ItemActionListener {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var context: Context
    lateinit var notiDBHelper: NotiDBHelper

    // 새로운 뷰 홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsAdapter.ItemViewHolder {
        context = parent.context
        sharedPreferences = context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)

        return ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_noti, parent, false))
    }

    // View 의 내용을 해당 포지션의 데이터로 바꿉니다.
    override fun onBindViewHolder(holder: NotificationsAdapter.ItemViewHolder, position: Int) {
        val dateTimeFormat = SimpleDateFormat("${context.getString(R.string.dateFormat)} ${context.getString(R.string.timeFormat)}", Locale.getDefault())
        val withDayTimeFormat = SimpleDateFormat("E ${context.getString(R.string.timeFormat)}", Locale.getDefault())
        val gmSansBold = Typeface.createFromAsset(context.assets, "fonts/gmsans_bold.otf")
        val gmSansMedium = Typeface.createFromAsset(context.assets, "fonts/gmsans_medium.otf")

        notiDBHelper = NotiDBHelper(context, NotiDBHelper.dbName, NotiDBHelper.dbVersion)

        holder.tvContentTitle.typeface = gmSansBold
        holder.tvContentText.typeface = gmSansMedium
        holder.tvTimeStamp.typeface = gmSansMedium

        holder.tvContentTitle.text = items[position].contentTitle
        holder.tvContentText.text = items[position].contentText

        holder.tvTimeStamp.text = items[position].timeStamp.let {
            val diff = kotlin.math.abs(it - System.currentTimeMillis())
            val daysDiff = TimeUnit.MILLISECONDS.toDays(diff)
            when {
                diff < 60 * 1000 -> context.getString(R.string.just_now)
                diff <= AlarmManager.INTERVAL_HOUR -> context.getString(R.string.minutes_format, TimeUnit.MILLISECONDS.toMinutes(diff))
                daysDiff < 1 -> context.getString(R.string.hours_format, TimeUnit.MILLISECONDS.toHours(diff))
                daysDiff <= 4 -> withDayTimeFormat.format(Date(items[position].timeStamp))
                else -> dateTimeFormat.format(Date(items[position].timeStamp))
            }
        }

        holder.wholeView.setBackgroundColor(if (items[position].isRead) Color.TRANSPARENT else ContextCompat.getColor(context, R.color.colorAccentAlpha))

        when (items[position].type) {
            NotificationItem.TYPE_REGISTER -> {
                holder.icon.setImageResource(R.drawable.ic_icon)
                holder.icon.contentDescription = context.getString(R.string.real_app_name)
            }
            NotificationItem.TYPE_FIREBASE -> {
                holder.icon.setImageResource(R.drawable.ic_icon)
                holder.icon.contentDescription = context.getString(R.string.real_app_name)
            }
            NotificationItem.TYPE_HOMEWORK -> {
                holder.icon.setImageResource(R.drawable.ic_assignment)
                holder.icon.contentDescription = context.getString(R.string.assignment)
            }

            NotificationItem.TYPE_LESSON -> {
                holder.icon.setImageResource(R.drawable.ic_video)
                holder.icon.contentDescription = context.getString(R.string.classtime)
            }

            NotificationItem.TYPE_SUP_LESSON -> {
                holder.icon.setImageResource(R.drawable.ic_video_sup)
                holder.icon.contentDescription = context.getString(R.string.classtime)
            }

            NotificationItem.TYPE_ZOOM -> {
                holder.icon.setImageResource(R.drawable.ic_live_class)
                holder.icon.contentDescription = context.getString(R.string.zoom)
            }
        }

        holder.wholeView.setOnClickListener {
            items[position].isRead = true
            notifyItemChanged(position)
            notiDBHelper.updateItemById(items[position])
            Intent(context, EditActivity::class.java).apply {
                putExtra("ID", items[position].destination)
                (context as Activity).startActivityForResult(this, REFRESH_MAIN_WORK)
            }
            NotificationsActivity.setTitleCount()
        }

        holder.wholeView.setOnLongClickListener {
            MyBottomSheetDialog(context).apply {
                dismissWithAnimation = true

                val view = layoutInflater.inflate(R.layout.dialog, LinearLayout(context), false)
                val imgLogo: ImageView = view.findViewById(R.id.imgLogo)
                val tvTitle: TextView = view.findViewById(R.id.tv_title)
                val tvContent: TextView = view.findViewById(R.id.tv_content)
                val btnPositive: LinearLayout = view.findViewById(R.id.btn_positive)
                val btnNegative: LinearLayout = view.findViewById(R.id.btn_negative)
                val tvPositive: TextView = view.findViewById(R.id.btn_positive_text)
                val tvNegative: TextView = view.findViewById(R.id.btn_negative_text)

                btnPositive.visibility = View.VISIBLE
                btnNegative.visibility = View.VISIBLE

                tvTitle.typeface = gmSansBold
                tvContent.typeface = gmSansMedium
                tvPositive.typeface = gmSansMedium
                tvNegative.typeface = gmSansMedium

                if (items[position].isRead) {
                    imgLogo.setImageResource(R.drawable.ic_mark_as_not_read)
                    tvTitle.text = context.getString(R.string.mark_as_not_read)
                    tvContent.text = context.getString(R.string.ask_mark_as_not_read)
                } else {
                    imgLogo.setImageResource(R.drawable.ic_mark_as_read)
                    tvTitle.text = context.getString(R.string.mark_as_read)
                    tvContent.text = context.getString(R.string.ask_mark_as_read)
                }

                btnPositive.setOnClickListener {
                    items[position].isRead = !items[position].isRead
                    notiDBHelper.updateItemById(items[position])
                    notifyItemChanged(position)
                    Snackbar.make(view, if (items[position].isRead) context.getString(R.string.marked_as_read) else context.getString(R.string.marked_as_not_read),
                        Snackbar.LENGTH_SHORT).setAction(R.string.undo) {
                        items[position].isRead = !items[position].isRead
                        notiDBHelper.updateItemById(items[position])
                        notifyItemChanged(position)
                    }.show()
                    NotificationsActivity.setTitleCount()
                    dismiss()
                }

                btnNegative.setOnClickListener {
                    dismiss()
                }

                setContentView(view)
            }.show()

            true
        }
    }

    fun setItemRead(position: Int) {
        items[position].isRead = true
        notiDBHelper.updateItemById(items[position])
        notifyItemChanged(position)
    }

    // 데이터 셋의 크기를 리턴해줍니다.
    override fun getItemCount(): Int = items.size

    override fun onItemSwiped(position: Int) {
        notiDBHelper.deleteData(items[position].id)
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val tvContentTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvContentText: TextView = itemView.findViewById(R.id.tv_content)
        val tvTimeStamp: TextView = itemView.findViewById(R.id.tv_time)
        val wholeView: ConstraintLayout = itemView.findViewById(R.id.wholeView)
    }
}