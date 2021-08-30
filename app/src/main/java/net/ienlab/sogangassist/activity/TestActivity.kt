package net.ienlab.sogangassist.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.adapter.MainWorkAdapter
import net.ienlab.sogangassist.adapter.MainWorkAdapter2
import net.ienlab.sogangassist.database.DBHelper
import net.ienlab.sogangassist.databinding.ActivityMainNewBinding
import net.ienlab.sogangassist.utils.PagerIndicator
import java.util.*
import kotlin.collections.ArrayList

class TestActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainNewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_new)
        binding.activity = this

//        binding.mainWorkList.findViewById<TextView>(R.id.tv_sub_name).isSelected = true

        val dbHelper = DBHelper(this, DBHelper.dbName, DBHelper.dbVersion)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.MONTH, 4)
            set(Calendar.DAY_OF_MONTH, 24)
        }
        val todayWork = dbHelper.getItemAtLastDate(calendar.timeInMillis).toMutableList().apply { sortWith( compareBy ({ it.isFinished }, {it.endTime}, {it.type} )) } as ArrayList

        Log.d(TAG, "${calendar.time} / ${todayWork.size}")

        binding.mainWorkList.adapter = MainWorkAdapter2(todayWork).apply {
//            setDeleteCallback(deleteCallbackListener)
//            setClickCallback(clickCallbackListener)
        }
        binding.mainWorkList.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        binding.mainWorkList.addItemDecoration(PagerIndicator(ContextCompat.getColor(applicationContext, R.color.colorAccent), ContextCompat.getColor(applicationContext, R.color.colorPrimary)))
        PagerSnapHelper().attachToRecyclerView(binding.mainWorkList)


    }
}