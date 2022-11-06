package net.ienlab.sogangassist.adapter

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Paint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.dinuscxj.progressbar.CircleProgressBar
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ienlab.sogangassist.BuildConfig
import net.ienlab.sogangassist.activity.*
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.constant.SharedKey
import net.ienlab.sogangassist.room.LMSDatabase
import net.ienlab.sogangassist.room.LMSEntity
import net.ienlab.sogangassist.utils.AppStorage
import net.ienlab.sogangassist.utils.ClickCallbackListener
import net.ienlab.sogangassist.utils.MyBottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainWorkAdapter(private var items: ArrayList<LMSEntity>) : RecyclerView.Adapter<MainWorkAdapter.ItemViewHolder>() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var context: Context
    lateinit var storage: AppStorage

//    var interstitialAd: InterstitialAd? = null
    private var deleteCallbackListener: ClickCallbackListener? = null
    private var clickCallbackListener: ClickCallbackListener? = null
    private var lmsDatabase: LMSDatabase? = null

    // 새로운 뷰 홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainWorkAdapter.ItemViewHolder {
        context = parent.context
        storage = AppStorage(context)
        lmsDatabase = LMSDatabase.getInstance(context)
        sharedPreferences = context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)

        return ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_main_work, parent, false))
    }

    // View 의 내용을 해당 포지션의 데이터로 바꿉니다.
    @OptIn(DelicateCoroutinesApi::class)
    override fun onBindViewHolder(holder: MainWorkAdapter.ItemViewHolder, position: Int) {
        val timeFormat = SimpleDateFormat(context.getString(R.string.timeFormat), Locale.getDefault())

//        setFullAd(context)

        holder.tvClass.text = "${items[holder.adapterPosition].className} · ${timeFormat.format(Date(items[holder.adapterPosition].endTime))}"

        val leftTime = items[holder.adapterPosition].endTime - System.currentTimeMillis()


        val hourLeft = TimeUnit.HOURS.convert(items[holder.adapterPosition].endTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS).toInt()
        val minuteLeft = TimeUnit.MINUTES.convert(items[holder.adapterPosition].endTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS).toInt() % 60
        val dayLeft = TimeUnit.DAYS.convert(items[holder.adapterPosition].endTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS).toInt()

        holder.tvTime.text = if (dayLeft != 0) context.getString(R.string.time_day, dayLeft)
        else if (hourLeft != 0 && minuteLeft != 0) context.getString(R.string.time_hour_min, hourLeft, minuteLeft)
        else if (hourLeft != 0) context.getString(R.string.time_hour, hourLeft)
        else context.getString(R.string.time_min, minuteLeft)

        holder.progressBar.max = (AlarmManager.INTERVAL_DAY / 1000).toInt()
        holder.progressBar.progress = (leftTime / 1000).toInt()
        holder.progressBar.setProgressFormatter { _, _ ->  "" }

        holder.itemView.setOnClickListener {
//            if (!storage.purchasedAds()) displayAd(context as Activity)
            clickCallbackListener?.callBack(holder.adapterPosition, items, this)
        }

        holder.itemView.setOnLongClickListener {
            MyBottomSheetDialog(context).apply {
                dismissWithAnimation = true

                val view = layoutInflater.inflate(R.layout.dialog, LinearLayout(this@MainWorkAdapter.context), false)
                val tvTitle: TextView = view.findViewById(R.id.tv_title)
                val tvContent: TextView = view.findViewById(R.id.tv_content)
                val btnPositive: LinearLayout = view.findViewById(R.id.btn_positive)
                val btnNegative: LinearLayout = view.findViewById(R.id.btn_negative)

                btnPositive.visibility = View.VISIBLE
                btnNegative.visibility = View.VISIBLE

                if (items[holder.adapterPosition].isFinished) {
                    tvTitle.text = this@MainWorkAdapter.context.getString(R.string.mark_as_not_finish)
                    tvContent.text = this@MainWorkAdapter.context.getString(R.string.ask_mark_as_not_finish)
                } else {
                    tvTitle.text = this@MainWorkAdapter.context.getString(R.string.mark_as_finish)
                    tvContent.text = this@MainWorkAdapter.context.getString(R.string.ask_mark_as_finish)
                }

                btnPositive.setOnClickListener {
                    items[holder.adapterPosition].isFinished = !items[holder.adapterPosition].isFinished
                    GlobalScope.launch(Dispatchers.IO) {
                        lmsDatabase?.getDao()?.update(items[holder.adapterPosition])
                        notifyItemChanged(holder.adapterPosition)
                        deleteCallbackListener?.callBack(holder.adapterPosition, items, this@MainWorkAdapter)
                        dismiss()
                    }
                }

                btnNegative.setOnClickListener {
                    dismiss()
                }

                setContentView(view)
            }.show()
            true
        }

        when (items[holder.adapterPosition].type) {
            LMSEntity.TYPE_HOMEWORK -> {
                holder.icIcon.setImageResource(R.drawable.ic_assignment)
                holder.icIcon.contentDescription = context.getString(R.string.assignment)
                holder.tvContent.text = items[holder.adapterPosition].homework_name
            }

            LMSEntity.TYPE_LESSON -> {
                holder.icIcon.setImageResource(R.drawable.ic_video)
                holder.icIcon.contentDescription = context.getString(R.string.classtime)
                holder.tvContent.text = context.getString(R.string.week_lesson_format, items[holder.adapterPosition].week, items[holder.adapterPosition].lesson)
            }

            LMSEntity.TYPE_SUP_LESSON -> {
                holder.icIcon.setImageResource(R.drawable.ic_video_sup)
                holder.icIcon.contentDescription = context.getString(R.string.sup_classtime)
                holder.tvContent.text = context.getString(R.string.week_lesson_format, items[holder.adapterPosition].week, items[holder.adapterPosition].lesson) + context.getString(R.string.enrich_study)
            }

            LMSEntity.TYPE_ZOOM -> {
                holder.icIcon.setImageResource(R.drawable.ic_live_class)
                holder.icIcon.contentDescription = context.getString(R.string.zoom)
                holder.tvContent.text = items[holder.adapterPosition].homework_name
            }

            LMSEntity.TYPE_TEAMWORK -> {
                holder.icIcon.setImageResource(R.drawable.ic_team)
                holder.icIcon.contentDescription = context.getString(R.string.team_project)
                holder.tvContent.text = items[holder.adapterPosition].homework_name
            }

            LMSEntity.TYPE_EXAM -> {
                holder.icIcon.setImageResource(R.drawable.ic_test)
                holder.icIcon.contentDescription = context.getString(R.string.exam)
                holder.tvContent.text = items[holder.adapterPosition].homework_name
            }
        }

        if (items[holder.adapterPosition].isFinished) {
            holder.tvClass.paintFlags = holder.tvClass.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvContent.paintFlags = holder.tvContent.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvTime.paintFlags = holder.tvTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvClass.alpha = 0.5f
            holder.tvContent.alpha = 0.5f
            holder.tvTime.alpha = 0.5f
            holder.icCheck.visibility = View.VISIBLE
        } else {
            holder.icCheck.visibility = View.GONE
        }

        holder.tvContent.isSelected = true

    }

    // 데이터 셋의 크기를 리턴해줍니다.
    override fun getItemCount(): Int = items.size

    fun setDeleteCallback(callbackListener: ClickCallbackListener?) {
        this.deleteCallbackListener = callbackListener
    }

    fun setClickCallback(callbackListener: ClickCallbackListener?) {
        this.clickCallbackListener = callbackListener
    }

    /*
    fun setFullAd(context: Context) {
        if (BuildConfig.DEBUG) {
            RequestConfiguration.Builder()
                .setTestDeviceIds(arrayListOf(testDevice)).apply {
                    MobileAds.setRequestConfiguration(build())
                }
        }

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, context.getString(R.string.full_ad_unit_id), adRequest, object: InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                interstitialAd = null
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
            }
        })
    }

    fun displayAd(activity: Activity) {
        val sharedPreferences = activity.getSharedPreferences(activity.packageName + "_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt(SharedKey.FULL_AD_CHARGE, sharedPreferences.getInt(SharedKey.FULL_AD_CHARGE, 0) + 1).apply()

        if (sharedPreferences.getInt(SharedKey.FULL_AD_CHARGE, 0) >= 3) {
            interstitialAd?.show(activity)
            sharedPreferences.edit().putInt(SharedKey.FULL_AD_CHARGE, 0).apply()
        }
    }

     */

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        val tvClass: TextView = itemView.findViewById(R.id.tv_class)
        val tvContent: TextView = itemView.findViewById(R.id.tv_content)
        val progressBar: CircleProgressBar = itemView.findViewById(R.id.progressBar)
        val icIcon: ImageView = itemView.findViewById(R.id.ic_icon)
        val icCheck: ImageView = itemView.findViewById(R.id.ic_check)
    }
}