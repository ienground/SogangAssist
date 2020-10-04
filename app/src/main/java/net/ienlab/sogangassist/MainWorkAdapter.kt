package net.ienlab.sogangassist

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class MainWorkAdapter(internal var mItems: MutableList<LMSClass>) : RecyclerView.Adapter<PostItemHolder>() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var context: Context

    // 새로운 뷰 홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostItemHolder {
        context = parent.context
        sharedPreferences = context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
        return PostItemHolder(LayoutInflater.from(context).inflate(R.layout.adapter_main_work, parent, false))
    }

    // View 의 내용을 해당 포지션의 데이터로 바꿉니다.
    override fun onBindViewHolder(holder: PostItemHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder")

        holder.class_name.text = mItems[position].className
        holder.wholeView.setOnClickListener {
            Intent(context, EditActivity::class.java).let {
                it.putExtra("ID", mItems[position].id)
                context.startActivity(it)
            }
        }
        when (mItems[position].type) {
            LMSType.HOMEWORK -> {
                holder.icon.setImageResource(R.drawable.ic_assignment)
                holder.sub_name.text = mItems[position].homework_name
            }

            LMSType.LESSON, LMSType.SUP_LESSON -> {
                holder.icon.setImageResource(R.drawable.ic_video)
                holder.sub_name.text = String.format("%d주 %d차시", mItems[position].week, mItems[position].lesson)
            }
        }

        if (mItems[position].isFinished) {
            holder.class_name.paintFlags = holder.class_name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.sub_name.paintFlags = holder.sub_name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }


    }


    // 데이터 셋의 크기를 리턴해줍니다.
    override fun getItemCount(): Int {
        return mItems.size
    }

}

class PostItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val icon: ImageView = itemView.findViewById(R.id.icon)
    val class_name: TextView = itemView.findViewById(R.id.class_name)
    val sub_name: TextView = itemView.findViewById(R.id.sub_name)
    val wholeView: ConstraintLayout = itemView.findViewById(R.id.wholeView)
}