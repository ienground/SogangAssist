package net.ienlab.sogangassist

import android.content.Context

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class OnboardingFragmentTabAdapter(fm: FragmentManager, internal var mContext: Context) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingFragment0.newInstance()
            1 -> OnboardingFragment1.newInstance()
            2 -> OnboardingFragment2.newInstance()
            3 -> OnboardingFragment3.newInstance()
            else -> OnboardingFragment0.newInstance()
        }
    }

    override fun getCount(): Int {
        return PAGE_NUMBER
    }

    companion object {
        internal const val PAGE_NUMBER = 4
    }

}
