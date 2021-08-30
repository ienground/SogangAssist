package net.ienlab.sogangassist.adapter

import android.app.Activity
import android.content.Context
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

class MainWorkAdapter2(private var items: ArrayList<LMSClass>) : RecyclerView.Adapter<MainWorkAdapter2.ItemViewHolder>() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var context: Context
    lateinit var dbHelper: DBHelper
    lateinit var storage: AppStorage

    var interstitialAd: InterstitialAd? = null
    private var deleteCallbackListener: ClickCallbackListener? = null
    private var clickCallbackListener: ClickCallbackListener? = null

    // 새로운 뷰 홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainWorkAdapter2.ItemViewHolder {
        context = parent.context
        sharedPreferences = context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)
        return ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_main_work2, parent, false))
    }

    // View 의 내용을 해당 포지션의 데이터로 바꿉니다.
    override fun onBindViewHolder(holder: MainWorkAdapter2.ItemViewHolder, position: Int) {
        val timeFormat = SimpleDateFormat(context.getString(R.string.timeFormat), Locale.getDefault())
        val typefaceBold = Typeface.createFromAsset(context.assets, "fonts/Pretendard-Black.otf")
        val typefaceRegular = Typeface.createFromAsset(context.assets, "fonts/Pretendard-Regular.otf")

        holder.tvTime.typeface = typefaceRegular
        holder.tvSubName.typeface = typefaceBold
        holder.tvClassName.typeface = typefaceRegular

        holder.tvTime.text = timeFormat.format(Date(items[holder.adapterPosition].endTime))
        holder.tvSubName.text = items[holder.adapterPosition].homework_name
        holder.tvClassName.text = items[holder.adapterPosition].className

        holder.tvSubName.isSelected = true

        setFullAd(context)

        storage = AppStorage(context)
        dbHelper = DBHelper(context, DBHelper.dbName, DBHelper.dbVersion)

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
    }
}