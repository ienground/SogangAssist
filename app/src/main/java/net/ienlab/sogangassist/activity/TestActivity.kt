package net.ienlab.sogangassist.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.adapter.MainWorkAdapter
import net.ienlab.sogangassist.database.DBHelper
import net.ienlab.sogangassist.databinding.ActivityEdit2Binding
import net.ienlab.sogangassist.databinding.ActivityMainNewBinding
import net.ienlab.sogangassist.utils.PagerIndicator
import java.util.*
import kotlin.collections.ArrayList

class TestActivity : AppCompatActivity() {

    lateinit var binding: ActivityEdit2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit2)

    }
}