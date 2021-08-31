package net.ienlab.sogangassist.adapter

import android.app.Activity
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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import net.ienlab.sogangassist.BuildConfig
import net.ienlab.sogangassist.activity.*
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.constant.SharedKey
import net.ienlab.sogangassist.data.LMSClass
import net.ienlab.sogangassist.database.DBHelper
import net.ienlab.sogangassist.utils.AppStorage
import net.ienlab.sogangassist.utils.ClickCallbackListener
import net.ienlab.sogangassist.utils.MyBottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

class MainWorkAdapter(private var items: ArrayList<LMSClass>) : RecyclerView.Adapter<MainWorkAdapter.ItemViewHolder>() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var context: Context
    lateinit var dbHelper: DBHelper
    lateinit var storage: AppStorage

    var interstitialAd: InterstitialAd? = null
    private var deleteCallbackListener: ClickCallbackListener? = null
    private var clickCallbackListener: ClickCallbackListener? = null

    // 새로운 뷰 홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainWorkAdapter.ItemViewHolder {
        context = parent.context
        storage = AppStorage(context)
        dbHelper = DBHelper(context, DBHelper.dbName, DBHelper.dbVersion)
        sharedPreferences = context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)

        return ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_main_work, parent, false))
    }

    // View 의 내용을 해당 포지션의 데이터로 바꿉니다.
    override fun onBindViewHolder(holder: MainWorkAdapter.ItemViewHolder, position: Int) {
        val timeFormat = SimpleDateFormat(context.getString(R.string.timeFormat), Locale.getDefault())
        val typefaceBold = Typeface.createFromAsset(context.assets, "fonts/Pretendard-Black.otf")
        val typefaceRegular = Typeface.createFromAsset(context.assets, "fonts/Pretendard-Regular.otf")

        setFullAd(context)

        holder.tvTime.typeface = typefaceRegular
        holder.tvSubName.typeface = typefaceBold
        holder.tvClassName.typeface = typefaceRegular

        holder.tvTime.text = context.getString(if (items[holder.adapterPosition].type != LMSClass.TYPE_ZOOM && items[holder.adapterPosition].type != LMSClass.TYPE_EXAM) R.string.deadline else R.string.start, timeFormat.format(Date(items[holder.adapterPosition].endTime)))
        holder.tvSubName.text = items[holder.adapterPosition].homework_name
        holder.tvClassName.text = items[holder.adapterPosition].className

        holder.itemView.setOnClickListener {
            if (!storage.purchasedAds()) displayAd(context as Activity)
            clickCallbackListener?.callBack(holder.adapterPosition, items, this)
        }

        holder.itemView.setOnLongClickListener {
            MyBottomSheetDialog(context).apply {
                dismissWithAnimation = true

                val view = layoutInflater.inflate(R.layout.dialog, LinearLayout(this@MainWorkAdapter.context), false)
                val imgLogo: ImageView = view.findViewById(R.id.imgLogo)
                val tvTitle: TextView = view.findViewById(R.id.tv_title)
                val tvContent: TextView = view.findViewById(R.id.tv_content)
                val btnPositive: LinearLayout = view.findViewById(R.id.btn_positive)
                val btnNegative: LinearLayout = view.findViewById(R.id.btn_negative)
                val tvPositive: TextView = view.findViewById(R.id.btn_positive_text)
                val tvNegative: TextView = view.findViewById(R.id.btn_negative_text)

                btnPositive.visibility = View.VISIBLE
                btnNegative.visibility = View.VISIBLE

                imgLogo.setImageResource(R.drawable.ic_check)
                tvTitle.typeface = typefaceBold
                tvContent.typeface = typefaceRegular
                tvPositive.typeface = typefaceRegular
                tvNegative.typeface = typefaceRegular

                if (items[holder.adapterPosition].isFinished) {
                    tvTitle.text = this@MainWorkAdapter.context.getString(R.string.mark_as_not_finish)
                    tvContent.text = this@MainWorkAdapter.context.getString(R.string.ask_mark_as_not_finish)
                } else {
                    tvTitle.text = this@MainWorkAdapter.context.getString(R.string.mark_as_finish)
                    tvContent.text = this@MainWorkAdapter.context.getString(R.string.ask_mark_as_finish)
                }

                btnPositive.setOnClickListener {
                    items[holder.adapterPosition].isFinished = !items[holder.adapterPosition].isFinished
                    dbHelper.updateItemById(items[holder.adapterPosition])
                    notifyItemChanged(holder.adapterPosition)
                    deleteCallbackListener?.callBack(holder.adapterPosition, items, this@MainWorkAdapter)
                    dismiss()
                }

                btnNegative.setOnClickListener {
                    dismiss()
                }

                setContentView(view)
            }.show()
            true
        }

        when (items[holder.adapterPosition].type) {
            LMSClass.TYPE_HOMEWORK -> {
                holder.icIcon.setImageResource(R.drawable.ic_assignment)
                holder.icIcon.contentDescription = context.getString(R.string.assignment)
                holder.tvSubName.text = items[holder.adapterPosition].homework_name
            }

            LMSClass.TYPE_LESSON -> {
                holder.icIcon.setImageResource(R.drawable.ic_video)
                holder.icIcon.contentDescription = context.getString(R.string.classtime)
                holder.tvSubName.text = context.getString(R.string.week_lesson_format, items[holder.adapterPosition].week, items[holder.adapterPosition].lesson)
            }

            LMSClass.TYPE_SUP_LESSON -> {
                holder.icIcon.setImageResource(R.drawable.ic_video_sup)
                holder.icIcon.contentDescription = context.getString(R.string.sup_classtime)
                holder.tvSubName.text = context.getString(R.string.week_lesson_format, items[holder.adapterPosition].week, items[holder.adapterPosition].lesson) + context.getString(R.string.enrich_study)
            }

            LMSClass.TYPE_ZOOM -> {
                holder.icIcon.setImageResource(R.drawable.ic_live_class)
                holder.icIcon.contentDescription = context.getString(R.string.zoom)
                holder.tvSubName.text = items[holder.adapterPosition].homework_name
            }

            LMSClass.TYPE_TEAMWORK -> {
                holder.icIcon.setImageResource(R.drawable.ic_team)
                holder.icIcon.contentDescription = context.getString(R.string.team_project)
                holder.tvSubName.text = items[holder.adapterPosition].homework_name
            }

            LMSClass.TYPE_EXAM -> {
                holder.icIcon.setImageResource(R.drawable.ic_test)
                holder.icIcon.contentDescription = context.getString(R.string.exam)
                holder.tvSubName.text = items[holder.adapterPosition].homework_name
            }
        }

        if (items[holder.adapterPosition].isFinished) {
            holder.tvClassName.paintFlags = holder.tvClassName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvSubName.paintFlags = holder.tvSubName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvTime.paintFlags = holder.tvTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvClassName.alpha = 0.5f
            holder.tvSubName.alpha = 0.5f
            holder.tvTime.alpha = 0.5f
            holder.icCheck.visibility = View.VISIBLE
        } else {
            holder.icCheck.visibility = View.GONE
        }
        val imgList = arrayOf(R.drawable.sg_img_01, R.drawable.sg_img_02, R.drawable.sg_img_03, R.drawable.sg_img_04)
        holder.itemView.setBackgroundResource(imgList[holder.adapterPosition % 4])

        holder.tvSubName.isSelected = true

    }

    // 데이터 셋의 크기를 리턴해줍니다.
    override fun getItemCount(): Int = items.size

    fun setDeleteCallback(callbackListener: ClickCallbackListener?) {
        this.deleteCallbackListener = callbackListener
    }

    fun setClickCallback(callbackListener: ClickCallbackListener?) {
        this.clickCallbackListener = callbackListener
    }

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

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        val tvSubName: TextView = itemView.findViewById(R.id.tv_sub_name)
        val tvClassName: TextView = itemView.findViewById(R.id.tv_class_name)
        val icIcon: ImageView = itemView.findViewById(R.id.ic_icon)
        val icCheck: ImageView = itemView.findViewById(R.id.ic_check)
    }
}