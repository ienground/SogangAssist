package net.ienlab.sogangassist.fragment

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import net.ienlab.sogangassist.constant.SharedKey
import net.ienlab.sogangassist.databinding.FragmentOnboarding3Binding
import net.ienlab.sogangassist.R

class OnboardingFragment3 : Fragment() {

    lateinit var binding: FragmentOnboarding3Binding

    private var mListener: OnFragmentInteractionListener? = null
    private val hours = arrayListOf(true, true, true, true, true)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_onboarding3, container, false)
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("${requireContext().packageName}_preferences", Context.MODE_PRIVATE)
        val buttons = listOf(binding.btn1hour, binding.btn2hour, binding.btn6hour, binding.btn12hour, binding.btn24hour)
        val sharedKeys = listOf(SharedKey.NOTIFY_1HOUR_HW, SharedKey.NOTIFY_2HOUR_HW, SharedKey.NOTIFY_6HOUR_HW, SharedKey.NOTIFY_12HOUR_HW, SharedKey.NOTIFY_24HOUR_HW)

        val typefaceBold = Typeface.createFromAsset(requireContext().assets, "fonts/Pretendard-Black.otf")
        val typefaceRegular = Typeface.createFromAsset(requireContext().assets, "fonts/Pretendard-Regular.otf")

        binding.tvPage.typeface = typefaceBold
        binding.tvTitle.typeface = typefaceBold
        binding.tvContent.typeface = typefaceRegular

        buttons.forEachIndexed { index, textView ->
            hours[index] = sharedPreferences.getBoolean(sharedKeys[index], true)
            textView.typeface = typefaceRegular
            textView.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val startColor = ContextCompat.getColor(requireContext(), if (hours[index]) R.color.white else android.R.color.transparent)
                    val endColor = ContextCompat.getColor(requireContext(), if (hours[index]) android.R.color.transparent else R.color.white)
                    val startTextColor = ContextCompat.getColor(requireContext(), if (hours[index]) R.color.color_ienlab_skyblue else R.color.white)
                    val endTextColor = ContextCompat.getColor(requireContext(), if (hours[index]) R.color.white else R.color.color_ienlab_skyblue)

                    ValueAnimator.ofArgb(startColor, endColor).apply {
                        duration = 300
                        addUpdateListener {
                            textView.backgroundTintList = ColorStateList.valueOf(it.animatedValue as Int)
                        }
                    }.start()
                    ValueAnimator.ofArgb(startTextColor, endTextColor).apply {
                        duration = 300
                        addUpdateListener {
                            textView.setTextColor(ColorStateList.valueOf(it.animatedValue as Int))
                        }
                    }.start()
                } else {
                    ValueAnimator.ofFloat(if (hours[index]) 1f else 0.3f, if (hours[index]) 0.3f else 1f).apply {
                        duration = 300
                        addUpdateListener {
                            textView.alpha = (it.animatedValue as Float)
                        }
                    }.start()
                }

                sharedPreferences.edit().putBoolean(sharedKeys[index], !hours[index]).apply()

                hours[index] = !hours[index]
            }
        }
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

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance() = OnboardingFragment3().apply {
            val args = Bundle()
            arguments = args
        }
    }
}

