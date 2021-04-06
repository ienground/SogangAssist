package net.ienlab.sogangassist.fragment

import android.animation.ValueAnimator
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import net.ienlab.sogangassist.constant.SharedKey
import net.ienlab.sogangassist.databinding.FragmentOnboarding5Binding
import net.ienlab.sogangassist.R

class OnboardingFragment5 : Fragment() {

    lateinit var binding: FragmentOnboarding5Binding
    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_onboarding5, container, false)
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("${requireContext().packageName}_preferences", Context.MODE_PRIVATE)
        val introBtnFine: ImageButton = requireActivity().findViewById(R.id.intro_btn_fine)
        val buttons = listOf(binding.btn1hour, binding.btn2hour, binding.btn6hour, binding.btn12hour, binding.btn24hour)
        val sharedKeys = listOf(SharedKey.NOTIFY_3MIN_ZOOM, SharedKey.NOTIFY_5MIN_ZOOM, SharedKey.NOTIFY_10MIN_ZOOM, SharedKey.NOTIFY_20MIN_ZOOM, SharedKey.NOTIFY_30MIN_ZOOM)

        buttons.forEachIndexed { index, imageButton ->
            sharedPreferences.edit().putBoolean(sharedKeys[index], false).apply()
            imageButton.setOnClickListener {
                ValueAnimator.ofFloat(if (hours[index]) 1f else 0.3f, if (hours[index]) 0.3f else 1f).apply {
                    duration = 300
                    addUpdateListener {
                        imageButton.alpha = (it.animatedValue as Float)
                    }
                }.start()
                sharedPreferences.edit().putBoolean(sharedKeys[index], !hours[index]).apply()

                hours[index] = !hours[index]

                with (introBtnFine) {
                    if (true in hours) {
                        isEnabled = true
                        alpha = 1f
                    } else {
                        isEnabled = false
                        alpha = 0.2f
                    }
                }
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
        fun newInstance() = OnboardingFragment5().apply {
            val args = Bundle()
            arguments = args
        }

        val hours = arrayListOf(false, false, false, false, false)
    }

}

