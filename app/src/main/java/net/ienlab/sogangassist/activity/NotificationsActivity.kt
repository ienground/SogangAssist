package net.ienlab.sogangassist.activity

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.snackbar.Snackbar
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.adapter.NotificationsAdapter
import net.ienlab.sogangassist.database.NotiDBHelper
import net.ienlab.sogangassist.databinding.ActivityNotificationsBinding
import net.ienlab.sogangassist.utils.ItemTouchHelperCallback
import net.ienlab.sogangassist.utils.MyBottomSheetDialog

class NotificationsActivity : AppCompatActivity() {

    lateinit var binding: ActivityNotificationsBinding

    lateinit var gmSansBold: Typeface
    lateinit var gmSansMedium: Typeface

    lateinit var adapter: NotificationsAdapter
    lateinit var notiDBHelper: NotiDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notifications)
        binding.activity = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        gmSansBold = Typeface.createFromAsset(assets, "fonts/gmsans_bold.otf")
        gmSansMedium = Typeface.createFromAsset(assets, "fonts/gmsans_medium.otf")

        binding.appTitle.typeface = gmSansBold
        binding.emptyMessage.typeface = gmSansMedium

        notiDBHelper = NotiDBHelper(this, NotiDBHelper.dbName, NotiDBHelper.dbVersion)

        adapter = NotificationsAdapter(notiDBHelper.getAllItem().apply { sortWith( compareByDescending { it.timeStamp }) })

        val itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(adapter))
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        binding.recyclerView.adapter = adapter
        binding.emptyMessage.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_notifications, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_delete_all -> {
                MyBottomSheetDialog(this).apply {
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

                    imgLogo.setImageResource(R.drawable.ic_clear_all)
                    tvTitle.typeface = gmSansBold
                    tvContent.typeface = gmSansMedium
                    tvPositive.typeface = gmSansMedium
                    tvNegative.typeface = gmSansMedium

                    tvTitle.text = context.getString(R.string.ask_delete_all)
                    tvContent.text = context.getString(R.string.ask_delete_all_msg)

                    btnPositive.setOnClickListener {
                        deleteDatabase(NotiDBHelper.dbName)
                        binding.recyclerView.visibility = View.GONE
                        binding.emptyMessage.visibility = View.VISIBLE
                        dismiss()
                    }

                    btnNegative.setOnClickListener {
                        dismiss()
                    }

                    setContentView(view)
                }.show()
            }
            R.id.menu_read_all -> {
                for (i in 0 until adapter.itemCount) {
                    adapter.setItemRead(i)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}