package net.ienlab.sogangassist

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_onboarding1.*


class OnboardingFragment1 : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val intro_btn_next: ImageButton = requireActivity().findViewById(R.id.intro_btn_next)
        val pm = requireContext().packageManager
        val lmsPackageName = "kr.co.imaxsoft.hellolms"

        section_label.typeface = Typeface.createFromAsset(requireContext().assets, "fonts/gmsans_bold.otf")
        section_content.typeface = Typeface.createFromAsset(requireContext().assets, "fonts/gmsans_medium.otf")

        if (isPackageInstalled(lmsPackageName, pm)) {
            with (btn_check_lms) {
                text = getString(R.string.lms_installed)
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                iconTint = ColorStateList.valueOf(Color.BLACK)
                setTextColor(Color.BLACK)
                isEnabled = false
            }
        } else {
            with (btn_check_lms) {
                setOnClickListener {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$lmsPackageName")))
                    } catch (e: ActivityNotFoundException) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$lmsPackageName")))
                    }
                }
            }
        }



    }

    fun isPackageInstalled(packageName: String, pm: PackageManager): Boolean {
        return try {
            pm.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
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
        fun newInstance() = OnboardingFragment1().apply {
            val args = Bundle()
            arguments = args
        }
    }

}

