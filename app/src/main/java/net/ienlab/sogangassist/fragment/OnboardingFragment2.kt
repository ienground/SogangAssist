package net.ienlab.sogangassist.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.databinding.FragmentOnboarding2Binding
import net.ienlab.sogangassist.utils.MyUtils


class OnboardingFragment2 : Fragment() {

    lateinit var binding: FragmentOnboarding2Binding
    private var mListener: OnFragmentInteractionListener? = null
//    lateinit var introBtnNext: ImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_onboarding2, container, false)
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val typefaceBold = ResourcesCompat.getFont(requireContext(), R.font.pretendard_black) ?: Typeface.DEFAULT
        val typefaceRegular = ResourcesCompat.getFont(requireContext(), R.font.pretendard_regular) ?: Typeface.DEFAULT

        binding.tvPage.typeface = typefaceBold
        binding.tvTitle.typeface = typefaceBold
        binding.btnAction.typeface = typefaceRegular

        binding.btnAction.setOnClickListener { startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")) }

        if (MyUtils.isNotiPermissionAllowed(requireContext())) {
            binding.btnAction.text = getString(R.string.noti_access_allowed)
            binding.btnAction.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
            binding.btnAction.isEnabled = false
        }
    }

    override fun onResume() {
        super.onResume()

        if (MyUtils.isNotiPermissionAllowed(requireContext())) {
            binding.btnAction.text = getString(R.string.noti_access_allowed)
            binding.btnAction.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
            binding.btnAction.isEnabled = false
        } else {
            binding.btnAction.text = getString(R.string.noti_access_allow)
            binding.btnAction.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_notification)
            binding.btnAction.isEnabled = true
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
        fun newInstance() = OnboardingFragment2().apply {
            val args = Bundle()
            arguments = args
        }
    }

}

