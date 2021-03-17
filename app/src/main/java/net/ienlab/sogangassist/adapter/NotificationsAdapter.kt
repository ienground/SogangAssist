package net.ienlab.sogangassist.adapter

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.*
import com.google.android.material.snackbar.Snackbar
import net.ienlab.sogangassist.BuildConfig
import net.ienlab.sogangassist.activity.*
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.constant.SharedGroup
import net.ienlab.sogangassist.data.LMSClass
import net.ienlab.sogangassist.data.NotificationItem
import net.ienlab.sogangassist.database.DBHelper
import net.ienlab.sogangassist.database.NotiDBHelper
import net.ienlab.sogangassist.utils.MyBottomSheetDialog
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationsAdapter(private var items: List<NotificationItem>) : RecyclerView.Adapter<NotificationsAdapter.ItemViewHolder>() {

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

        // 1시간 전까지는 %d분 전
        // 15시간 전까지는 %d시간 전
        // 어제 날짜까지는 어제 몇시 몇분
        // 4일 전까지는 요일 몇시 몇분
        // 그 이상은 날짜

        holder.tvTimeStamp.text = items[position].timeStamp.let {
            val diff = kotlin.math.abs(it - System.currentTimeMillis())
//            val now = Calendar.getInstance()
//            val timeStampCalendar = Calendar.getInstance().apply { timeInMillis = it }
            val daysDiff = TimeUnit.MILLISECONDS.toDays(diff)
            when {
                diff <= AlarmManager.INTERVAL_HOUR -> {
                    "${TimeUnit.MILLISECONDS.toMinutes(diff)}분 전"
                }
                daysDiff <= 1 -> {
                    "${TimeUnit.MILLISECONDS.toHours(diff)}시간 전"
                }
                daysDiff <= 4 -> {
                    withDayTimeFormat.format(Date(items[position].timeStamp))
                }
                else -> {
                    dateTimeFormat.format(Date(items[position].timeStamp))
                }
            }
        }

//        holder.wholeView.setBackgroundColor()


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
                holder.icon.setImageResource(R.drawable.ic_groups)
                holder.icon.contentDescription = context.getString(R.string.zoom)
            }
        }
    }


    // 데이터 셋의 크기를 리턴해줍니다.
    override fun getItemCount(): Int {
        return items.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val tvContentTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvContentText: TextView = itemView.findViewById(R.id.tv_content)
        val tvTimeStamp: TextView = itemView.findViewById(R.id.tv_time)
        val wholeView: ConstraintLayout = itemView.findViewById(R.id.wholeView)
    }
}