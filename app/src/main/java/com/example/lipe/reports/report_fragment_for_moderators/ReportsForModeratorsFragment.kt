package com.example.lipe.reports.report_fragment_for_moderators

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.lipe.R
import com.example.lipe.chats_and_groups.ChatsAndGroupsTabAdapter
import com.example.lipe.databinding.FragmentReportsForModeratorsBinding
import com.google.android.material.tabs.TabLayout

class ReportsForModeratorsFragment : Fragment() {

    private lateinit var binding: FragmentReportsForModeratorsBinding
    private lateinit var adapter: EventReportTabAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportsForModeratorsBinding.inflate(inflater, container, false)

        adapter = EventReportTabAdapter(childFragmentManager, lifecycle)
        binding.viewPager.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tabLayout.apply {
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    binding.viewPager.currentItem = tab!!.position
                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {}

                override fun onTabReselected(p0: TabLayout.Tab?) {}
            })
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })
    }
}