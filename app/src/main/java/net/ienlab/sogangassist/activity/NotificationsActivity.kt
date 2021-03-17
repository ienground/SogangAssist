package net.ienlab.sogangassist.activity

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.adapter.NotificationsAdapter
import net.ienlab.sogangassist.database.NotiDBHelper
import net.ienlab.sogangassist.databinding.ActivityNotificationsBinding

class NotificationsActivity : AppCompatActivity() {

    lateinit var binding: ActivityNotificationsBinding

    lateinit var gmSansBold: Typeface
    lateinit var gmSansMedium: Typeface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notifications)
        binding.activity = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        val notiDBHelper = NotiDBHelper(this, NotiDBHelper.dbName, NotiDBHelper.dbVersion)
        binding.recyclerView.adapter = NotificationsAdapter(notiDBHelper.getAllItem().sortedWith( compareByDescending { it.timeStamp }))
    }
}