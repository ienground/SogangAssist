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
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_onboarding2.*


class OnboardingFragment2 : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null
    lateinit var intro_btn_next: ImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intro_btn_next = requireActivity().findViewById(R.id.intro_btn_next)

        section_label.typeface = Typeface.createFromAsset(requireContext().assets, "fonts/gmsans_bold.otf")
        section_content.typeface = Typeface.createFromAsset(requireContext().assets, "fonts/gmsans_medium.otf")

        if (isNotiPermissionAllowed()) {
            with (btn_noti_access) {
                text = getString(R.string.noti_access_allowed)
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                iconTint = ColorStateList.valueOf(Color.BLACK)
                setTextColor(Color.BLACK)
                isEnabled = false
            }

            intro_btn_next.alpha = 1.0f
            intro_btn_next.isEnabled = true
        }

        btn_noti_access.setOnClickListener {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }

    }

    fun isNotiPermissionAllowed(): Boolean {
        val notiListenerSet = NotificationManagerCompat.getEnabledListenerPackages(requireContext())
        val myPackageName = requireActivity().packageName
        for (packageName in notiListenerSet) {
            if (packageName == null) {
                continue
            }
            if (packageName == myPackageName) {
                return true
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        if (isNotiPermissionAllowed()) {
            with (btn_noti_access) {
                text = getString(R.string.noti_access_allowed)
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                iconTint = ColorStateList.valueOf(Color.BLACK)
                setTextColor(Color.BLACK)
                isEnabled = false
            }

            intro_btn_next.alpha = 1.0f
            intro_btn_next.isEnabled = true
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

