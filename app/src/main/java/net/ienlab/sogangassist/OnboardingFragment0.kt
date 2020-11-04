package net.ienlab.sogangassist

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import net.ienlab.sogangassist.databinding.FragmentOnboarding0Binding

class OnboardingFragment0 : Fragment() {

    lateinit var binding: FragmentOnboarding0Binding
    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_onboarding0, container, false)
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sectionLabel.typeface = Typeface.createFromAsset(requireContext().assets, "fonts/gmsans_bold.otf")
        binding.sectionContent.typeface = Typeface.createFromAsset(requireContext().assets, "fonts/gmsans_medium.otf")

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
        fun newInstance() = OnboardingFragment0().apply {
            val args = Bundle()
            arguments = args
        }
    }
}