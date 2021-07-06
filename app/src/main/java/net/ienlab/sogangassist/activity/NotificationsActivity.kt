package net.ienlab.sogangassist.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.snackbar.Snackbar
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.adapter.NotificationsAdapter
import net.ienlab.sogangassist.data.NotificationItem
import net.ienlab.sogangassist.database.NotiDBHelper
import net.ienlab.sogangassist.databinding.ActivityNotificationsBinding
import net.ienlab.sogangassist.utils.ItemTouchHelperCallback
import net.ienlab.sogangassist.utils.MyBottomSheetDialog
import net.ienlab.sogangassist.utils.NotiClickCallbackListener

class NotificationsActivity : AppCompatActivity() {

    lateinit var gmSansBold: Typeface
    lateinit var gmSansMedium: Typeface

    lateinit var adapter: NotificationsAdapter
    lateinit var notiDBHelper: NotiDBHelper
    lateinit var binding: ActivityNotificationsBinding

    private val clickCallbackListener = object: NotiClickCallbackListener {
        override fun callBack(position: Int, items: List<NotificationItem>, adapter: NotificationsAdapter) {
            Intent(applicationContext, EditActivity::class.java).apply {
                putExtra("ID", items[position].destination)

                if (notiDBHelper.checkIsDataAlreadyInDBorNot(NotiDBHelper.ID, items[position].destination.toString())) {
                    startActivity(this)
                } else {
                    Snackbar.make(window.decorView.rootView, getString(R.string.err_date_deleted), Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val titleCountCallbackListener = object: NotiClickCallbackListener {
        override fun callBack(position: Int, items: List<NotificationItem>, adapter: NotificationsAdapter) {
            setTitleCount()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notifications)
        binding.activity = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        gmSansBold = Typeface.createFromAsset(assets, "fonts/gmsans_bold.otf")
        gmSansMedium = Typeface.createFromAsset(assets, "fonts/gmsans_medium.otf")

        binding.appTitle.typeface = gmSansBold
        binding.emptyMessage.typeface = gmSansMedium

        notiDBHelper = NotiDBHelper(this, NotiDBHelper.dbName, NotiDBHelper.dbVersion)

        val data = notiDBHelper.getAllItem().apply { sortWith( compareByDescending { it.timeStamp }) }
        adapter = NotificationsAdapter(data).apply {
            setClickCallbackListener(clickCallbackListener)
            setTitleCountCallbackListener(titleCountCallbackListener)
        }

        val itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(adapter))
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        binding.recyclerView.adapter = adapter
        binding.emptyMessage.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE

        setTitleCount()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_notifications, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(Activity.RESULT_OK)
                super.onBackPressed()
            }
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
                        setTitleCount()
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
                setTitleCount()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setTitleCount() {
        val data = notiDBHelper.getAllItem()
        var unreadCount = 0
        data.forEach { if (!it.isRead) unreadCount++ }

        binding.appTitle.text = when (unreadCount) {
            0 -> binding.activity?.getString(R.string.no_new_noti)
            1 -> binding.activity?.getString(R.string.new_a_noti)
            else -> binding.activity?.getString(R.string.new_noti, unreadCount)
        }
    }
}