package net.ienlab.sogangassist

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import net.ienlab.sogangassist.databinding.FragmentOnboarding2Binding


class OnboardingFragment2 : Fragment() {

    lateinit var binding: FragmentOnboarding2Binding
    private var mListener: OnFragmentInteractionListener? = null
    lateinit var introBtnNext: ImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_onboarding2, container, false)
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        introBtnNext = requireActivity().findViewById(R.id.intro_btn_next)

        binding.sectionLabel.typeface = Typeface.createFromAsset(requireContext().assets, "fonts/gmsans_bold.otf")
        binding.sectionContent.typeface = Typeface.createFromAsset(requireContext().assets, "fonts/gmsans_medium.otf")

        if (MyUtils.isNotiPermissionAllowed(requireContext())) {
            with (binding.btnNotiAccess) {
                text = getString(R.string.noti_access_allowed)
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                iconTint = ColorStateList.valueOf(Color.BLACK)
                setTextColor(Color.BLACK)
                isEnabled = false
            }

            introBtnNext.alpha = 1.0f
            introBtnNext.isEnabled = true
        }

        binding.btnNotiAccess.setOnClickListener {
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }

    }

    override fun onResume() {
        super.onResume()
        if (MyUtils.isNotiPermissionAllowed(requireContext())) {
            with (binding.btnNotiAccess) {
                text = getString(R.string.noti_access_allowed)
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                iconTint = ColorStateList.valueOf(Color.BLACK)
                setTextColor(Color.BLACK)
                isEnabled = false
            }

            introBtnNext.alpha = 1.0f
            introBtnNext.isEnabled = true
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

