package net.ienlab.sogangassist

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_onboarding4.*

class OnboardingFragment4 : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding4, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("${requireContext().packageName}_preferences", Context.MODE_PRIVATE)
        val inflator = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val intro_btn_fine: ImageButton = requireActivity().findViewById(R.id.intro_btn_fine)

        section_label.typeface = Typeface.createFromAsset(requireContext().assets, "fonts/gmsans_bold.otf")
        section_content.typeface = Typeface.createFromAsset(requireContext().assets, "fonts/gmsans_medium.otf")

        btn_1hour.setOnClickListener {
            if (hours[0]) {
                hours[0] = false
                btn_1hour.alpha = 0.3f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_1HOUR_LEC, false).apply()
            } else {
                hours[0] = true
                btn_1hour.alpha = 1.0f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_1HOUR_LEC, true).apply()
            }

            with (intro_btn_fine) {
                if (true in hours) {
                    isEnabled = true
                    alpha = 1f
                } else {
                    isEnabled = false
                    alpha = 0.2f
                }
            }
        }

        btn_2hour.setOnClickListener {
            if (hours[1]) {
                hours[1] = false
                btn_2hour.alpha = 0.3f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_2HOUR_LEC, false).apply()
            } else {
                hours[1] = true
                btn_2hour.alpha = 1.0f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_2HOUR_LEC, true).apply()
            }

            with (intro_btn_fine) {
                if (true in hours) {
                    isEnabled = true
                    alpha = 1f
                } else {
                    isEnabled = false
                    alpha = 0.2f
                }
            }
        }

        btn_6hour.setOnClickListener {
            if (hours[2]) {
                hours[2] = false
                btn_6hour.alpha = 0.3f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_6HOUR_LEC, false).apply()
            } else {
                hours[2] = true
                btn_6hour.alpha = 1.0f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_6HOUR_LEC, true).apply()
            }

            with (intro_btn_fine) {
                if (true in hours) {
                    isEnabled = true
                    alpha = 1f
                } else {
                    isEnabled = false
                    alpha = 0.2f
                }
            }
        }

        btn_12hour.setOnClickListener {
            if (hours[3]) {
                hours[3] = false
                btn_12hour.alpha = 0.3f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_12HOUR_LEC, false).apply()
            } else {
                hours[3] = true
                btn_12hour.alpha = 1.0f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_12HOUR_LEC, true).apply()
            }

            with (intro_btn_fine) {
                if (true in hours) {
                    isEnabled = true
                    alpha = 1f
                } else {
                    isEnabled = false
                    alpha = 0.2f
                }
            }
        }

        btn_24hour.setOnClickListener {
            if (hours[4]) {
                hours[4] = false
                btn_24hour.alpha = 0.3f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_24HOUR_LEC, false).apply()
            } else {
                hours[4] = true
                btn_24hour.alpha = 1.0f
                sharedPreferences.edit().putBoolean(SharedGroup.NOTIFY_24HOUR_LEC, true).apply()
            }

            with (intro_btn_fine) {
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
        fun newInstance() = OnboardingFragment4().apply {
            val args = Bundle()
            arguments = args
        }

        val hours = mutableListOf(false, false, false, false, false)
    }

}

