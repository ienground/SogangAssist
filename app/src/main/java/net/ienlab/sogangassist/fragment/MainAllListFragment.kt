package net.ienlab.sogangassist.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import kotlinx.coroutines.*
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.activity.EditActivity
import net.ienlab.sogangassist.adapter.MainLMSAdapter
import net.ienlab.sogangassist.callback.MainAllListClickCallbackListener
import net.ienlab.sogangassist.constant.IntentKey
import net.ienlab.sogangassist.databinding.FragmentMainAllListBinding
import net.ienlab.sogangassist.room.LMSDatabase
import net.ienlab.sogangassist.room.LMSEntity
import net.ienlab.sogangassist.utils.MyUtils.Companion.timeZero
import net.ienlab.sogangassist.utils.MyUtils.Companion.tomorrowZero
import java.util.*

class MainAllListFragment() : Fragment() {

    var calendar: Calendar

    init {
        this.calendar = Calendar.getInstance()
    }

    constructor(calendar: Calendar) : this() {
        this.calendar = calendar
    }

    lateinit var binding: FragmentMainAllListBinding

    private var mListener: OnFragmentInteractionListener? = null
    private var lmsDatabase: LMSDatabase? = null

    private val clickCallbackListener = object: MainAllListClickCallbackListener {
        override fun callBack(position: Int, data: LMSEntity) {
            mListener?.onPlanListItemClicked(position, data)
            editActivityLauncher.launch(Intent(context, EditActivity::class.java).apply {
                putExtra(IntentKey.ITEM_ID, data.id)
            })
        }
    }

    // StartActivityForResult
    private lateinit var editActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_all_list, container, false)
        binding.fragment = this

        return binding.root
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lmsDatabase = LMSDatabase.getInstance(requireContext())
//skejt ghdtiloh lketj gkldj l;v j,trlhytmjyorklgsdfxmf v;xljvyhklrjkfp[askd l;
//XURY 최고 우주 지배]


        editActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                setData()
                mListener?.onPlanListItemEdited(result.data?.getLongExtra(IntentKey.ENDTIME, -1L) ?: -1L)
            }
        }

        setData()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun setData() {
        GlobalScope.launch(Dispatchers.IO) {
            val data: ArrayList<LMSEntity> = arrayListOf()

            data.addAll(lmsDatabase?.getDao()?.getByEndTime(calendar.timeZero().timeInMillis, calendar.tomorrowZero().timeInMillis) ?: listOf())
            data.sortWith(compareBy( { it.endTime } , { it.startTime } ))

            val adapter = MainLMSAdapter(data, calendar).apply {
                setCallbackListener(clickCallbackListener)
            }

            withContext(Dispatchers.Main) {
                binding.allList.adapter = adapter
            }
        }
    }

    interface OnFragmentInteractionListener {
        fun onPlanListItemClicked(position: Int, data: LMSEntity)
        fun onPlanListItemEdited(endTime: Long)
    }
}