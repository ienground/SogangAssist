package net.ienlab.sogangassist.adapter

import android.app.Activity
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
import net.ienlab.sogangassist.database.DBHelper
import net.ienlab.sogangassist.database.dbName
import net.ienlab.sogangassist.database.dbVersion
import net.ienlab.sogangassist.utils.MyBottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

class MainWorkAdapter(private var items: MutableList<LMSClass>) : RecyclerView.Adapter<MainWorkAdapter.ItemViewHolder>() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var context: Context
    lateinit var interstitialAd: InterstitialAd
    lateinit var dbHelper: DBHelper

    // 새로운 뷰 홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainWorkAdapter.ItemViewHolder {
        context = parent.context
        sharedPreferences = context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)
        return ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_main_work, parent, false))
    }

    // View 의 내용을 해당 포지션의 데이터로 바꿉니다.
    override fun onBindViewHolder(holder: MainWorkAdapter.ItemViewHolder, position: Int) {
        val timeFormat = SimpleDateFormat(context.getString(R.string.timeFormat), Locale.getDefault())
        val gmSansBold = Typeface.createFromAsset(context.assets, "fonts/gmsans_bold.otf")
        val gmSansMedium = Typeface.createFromAsset(context.assets, "fonts/gmsans_medium.otf")

        setFullAd(context)

        dbHelper = DBHelper(context, dbName, dbVersion)

        holder.class_name.typeface = gmSansBold
        holder.sub_name.typeface = gmSansBold
        holder.end_time.typeface = gmSansMedium

        holder.class_name.text = items[position].className
        holder.end_time.text =
            context.getString(if (items[position].type != LMSClass.TYPE_ZOOM) R.string.deadline else R.string.start, timeFormat.format(Date(items[position].endTime)))
        holder.wholeView.setOnClickListener {
            Intent(context, EditActivity::class.java).apply {
                putExtra("ID", items[position].id)
                displayAd(context)
                (context as Activity).startActivityForResult(this, REFRESH_MAIN_WORK)
            }
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

                imgLogo.setImageResource(R.drawable.ic_check)
                tvTitle.typeface = gmSansBold
                tvContent.typeface = gmSansMedium
                tvPositive.typeface = gmSansMedium
                tvNegative.typeface = gmSansMedium

                if (items[position].isFinished) {
                    tvTitle.text = context.getString(R.string.mark_as_not_finish)
                    tvContent.text = context.getString(R.string.ask_mark_as_not_finish)
                } else {
                    tvTitle.text = context.getString(R.string.mark_as_finish)
                    tvContent.text = context.getString(R.string.ask_mark_as_finish)
                }


                btnPositive.setOnClickListener {
                    items[position].isFinished = !items[position].isFinished
                    dbHelper.updateItemById(items[position])
                     notifyItemChanged(position)
                    Snackbar.make(MainActivity.view, if (items[position].isFinished) context.getString(R.string.marked_as_finish) else context.getString(R.string.marked_as_not_finish),
                        Snackbar.LENGTH_SHORT).setAction(R.string.undo) {
                        items[position].isFinished = !items[position].isFinished
                        dbHelper.updateItemById(items[position])
                        notifyItemChanged(position)
                    }.show()
                    dismiss()
                }

                btnNegative.setOnClickListener {
                    dismiss()
                }

                setContentView(view)
            }.show()
            true
        }

        when (items[position].type) {
            LMSClass.TYPE_HOMEWORK -> {
                holder.icon.setImageResource(R.drawable.ic_assignment)
                holder.icon.contentDescription = context.getString(R.string.assignment)
                holder.sub_name.text = items[position].homework_name
            }

            LMSClass.TYPE_LESSON -> {
                holder.icon.setImageResource(R.drawable.ic_video)
                holder.icon.contentDescription = context.getString(R.string.classtime)
                holder.sub_name.text = context.getString(R.string.week_lesson_format, items[position].week, items[position].lesson)
            }

            LMSClass.TYPE_SUP_LESSON -> {
                holder.icon.setImageResource(R.drawable.ic_video_sup)
                holder.icon.contentDescription = context.getString(R.string.classtime)
                holder.sub_name.text = context.getString(R.string.week_lesson_format, items[position].week, items[position].lesson) + context.getString(R.string.enrich_study)
            }

            LMSClass.TYPE_ZOOM -> {
                holder.icon.setImageResource(R.drawable.ic_groups)
                holder.icon.contentDescription = context.getString(R.string.zoom)
                holder.sub_name.text = items[position].homework_name
            }
        }

        if (items[position].isFinished) {
            holder.class_name.paintFlags = holder.class_name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.sub_name.paintFlags = holder.sub_name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.end_time.paintFlags = holder.end_time.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.check.visibility = View.VISIBLE
        } else {
            holder.check.visibility = View.GONE
        }


    }


    // 데이터 셋의 크기를 리턴해줍니다.
    override fun getItemCount(): Int {
        return items.size
    }

    fun setFullAd(context: Context) {
        interstitialAd = InterstitialAd(context)
        interstitialAd.adUnitId = context.getString(R.string.full_ad_unit_id)
        val adRequest2 = AdRequest.Builder()
        if (BuildConfig.DEBUG) {
            RequestConfiguration.Builder()
                .setTestDeviceIds(mutableListOf(testDevice)).let {
                    MobileAds.setRequestConfiguration(it.build())
                }
        }

        interstitialAd.loadAd(adRequest2.build())
        interstitialAd.adListener = object : AdListener() { //전면 광고의 상태를 확인하는 리스너 등록
            override fun onAdClosed() { //전면 광고가 열린 뒤에 닫혔을 때
                val adRequest3 = AdRequest.Builder()
                if (BuildConfig.DEBUG) {
                    RequestConfiguration.Builder()
                        .setTestDeviceIds(mutableListOf(testDevice)).let {
                            MobileAds.setRequestConfiguration(it.build())
                        }
                }
                interstitialAd.loadAd(adRequest3.build())
            }
        }
    }

    fun displayAd(context: Context) {
        val sharedPreferences = context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt(
            SharedGroup.FULL_AD_CHARGE,
            sharedPreferences.getInt(SharedGroup.FULL_AD_CHARGE, 0) + 1).apply()
        Log.d("AdTAG", "ad:" + sharedPreferences.getInt(SharedGroup.FULL_AD_CHARGE, 0))
        Log.d("AdTAG", "isLoaded:" + interstitialAd.isLoaded)

        if (interstitialAd.isLoaded && sharedPreferences.getInt(SharedGroup.FULL_AD_CHARGE, 0) >= 3) {
            interstitialAd.show()
            sharedPreferences.edit().putInt(SharedGroup.FULL_AD_CHARGE, 0).apply()
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val class_name: TextView = itemView.findViewById(R.id.class_name)
        val sub_name: TextView = itemView.findViewById(R.id.sub_name)
        val end_time: TextView = itemView.findViewById(R.id.end_time)
        val check: ImageView = itemView.findViewById(R.id.check)
        val wholeView: ConstraintLayout = itemView.findViewById(R.id.wholeView)
    }
}