package net.ienlab.sogangassist

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import net.ienlab.sogangassist.databinding.FragmentOnboarding3Binding

class OnboardingFragment3 : Fragment() {

    lateinit var binding: FragmentOnboarding3Binding
    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_onboarding3, container, false)
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("${requireContext().packageName}_preferences", Context.MODE_PRIVATE)
        val introBtnNext: ImageButton = requireActivity().findViewById(R.id.intro_btn_next)

        binding.btn1hour.setOnClickListener {
            if (hours[0]) {
                hours[0] = false
                binding.btn1hour.alpha = 0.3f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_1HOUR_HW, false).apply()
            } else {
                hours[0] = true
                binding.btn1hour.alpha = 1.0f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_1HOUR_HW, true).apply()
            }

            with (introBtnNext) {
                if (true in hours) {
                    isEnabled = true
                    alpha = 1f
                } else {
                    isEnabled = false
                    alpha = 0.2f
                }
            }
        }

        binding.btn2hour.setOnClickListener {
            if (hours[1]) {
                hours[1] = false
                binding.btn2hour.alpha = 0.3f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_2HOUR_HW, false).apply()
            } else {
                hours[1] = true
                binding.btn2hour.alpha = 1.0f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_2HOUR_HW, true).apply()
            }

            with (introBtnNext) {
                if (true in hours) {
                    isEnabled = true
                    alpha = 1f
                } else {
                    isEnabled = false
                    alpha = 0.2f
                }
            }
        }

        binding.btn6hour.setOnClickListener {
            if (hours[2]) {
                hours[2] = false
                binding.btn6hour.alpha = 0.3f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_6HOUR_HW, false).apply()
            } else {
                hours[2] = true
                binding.btn6hour.alpha = 1.0f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_6HOUR_HW, true).apply()
            }

            with (introBtnNext) {
                if (true in hours) {
                    isEnabled = true
                    alpha = 1f
                } else {
                    isEnabled = false
                    alpha = 0.2f
                }
            }
        }

        binding.btn12hour.setOnClickListener {
            if (hours[3]) {
                hours[3] = false
                binding.btn12hour.alpha = 0.3f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_12HOUR_HW, false).apply()
            } else {
                hours[3] = true
                binding.btn12hour.alpha = 1.0f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_12HOUR_HW, true).apply()
            }

            with (introBtnNext) {
                if (true in hours) {
                    isEnabled = true
                    alpha = 1f
                } else {
                    isEnabled = false
                    alpha = 0.2f
                }
            }
        }

        binding.btn24hour.setOnClickListener {
            if (hours[4]) {
                hours[4] = false
                binding.btn24hour.alpha = 0.3f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_24HOUR_HW, false).apply()
            } else {
                hours[4] = true
                binding.btn24hour.alpha = 1.0f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_24HOUR_HW, true).apply()
            }

            with (introBtnNext) {
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

        val hours = mutableListOf(false, false, false, false, false)
    }

}

