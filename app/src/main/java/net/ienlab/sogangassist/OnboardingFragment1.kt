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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import net.ienlab.sogangassist.databinding.FragmentOnboarding1Binding

class OnboardingFragment1 : Fragment() {

    lateinit var binding: FragmentOnboarding1Binding
    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_onboarding1, container, false)
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gmSansBold = Typeface.createFromAsset(requireContext().assets, "fonts/gmsans_bold.otf")
        val gmSansMedium = Typeface.createFromAsset(requireContext().assets, "fonts/gmsans_medium.otf")

        val pm = requireContext().packageManager
        val lmsPackageName = "kr.co.imaxsoft.hellolms"

        binding.tvInstall.typeface = gmSansMedium
        if (isPackageInstalled(lmsPackageName, pm)) {
            binding.tvInstall.text = getString(R.string.lms_installed)
            binding.icInstall.setImageResource(R.drawable.ic_check_circle)
        } else {
            binding.groupInstall.setOnClickListener {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$lmsPackageName")))
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$lmsPackageName")))
                }
            }
        }
    }

    fun isPackageInstalled(packageName: String, pm: PackageManager): Boolean {
        return try {
            pm.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
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

