package net.ienlab.sogangassist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dinuscxj.progressbar.CircleProgressBar
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.room.LMSEntity

class MainEventAdapter(private var items: ArrayList<LMSEntity>): RecyclerView.Adapter<MainEventAdapter.ItemViewHolder>() {

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        context = parent.context
        return ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_main_event, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

    }

    inner class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvContentName: TextView = itemView.findViewById(R.id.tv_content_name)
        val tvClassName: TextView = itemView.findViewById(R.id.tv_class_name)
        val progressBar: CircleProgressBar = itemView.findViewById(R.id.progressBar)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
    }
}