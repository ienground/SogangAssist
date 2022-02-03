package net.ienlab.sogangassist.fragment

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.databinding.FragmentOnboarding1Binding

class OnboardingFragment1 : Fragment() {

    lateinit var binding: FragmentOnboarding1Binding

    lateinit var pm: PackageManager

    private var mListener: OnFragmentInteractionListener? = null
    private val lmsPackageName = "kr.co.imaxsoft.hellolms"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_onboarding1, container, false)
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val typefaceBold = ResourcesCompat.getFont(requireContext(), R.font.pretendard_black) ?: Typeface.DEFAULT
        val typefaceRegular = ResourcesCompat.getFont(requireContext(), R.font.pretendard_regular) ?: Typeface.DEFAULT

        pm = requireContext().packageManager

        binding.tvPage.typeface = typefaceBold
        binding.tvTitle.typeface = typefaceBold
        binding.btnAction.typeface = typefaceRegular

        binding.btnAction.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$lmsPackageName")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$lmsPackageName")))
            }
        }

        if (isPackageInstalled(lmsPackageName, pm)) {
            binding.btnAction.text = getString(R.string.lms_installed)
            binding.btnAction.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
            binding.btnAction.isEnabled = false
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

    override fun onResume() {
        super.onResume()
        if (isPackageInstalled(lmsPackageName, pm)) {
            binding.btnAction.text = getString(R.string.lms_installed)
            binding.btnAction.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
            binding.btnAction.isEnabled = false
        } else {
            binding.btnAction.text = getString(R.string.lms_install)
            binding.btnAction.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_store)
            binding.btnAction.isEnabled = true
        }
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

