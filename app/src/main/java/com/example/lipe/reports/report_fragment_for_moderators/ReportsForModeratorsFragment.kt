package com.example.lipe.reports.report_fragment_for_moderators

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.lipe.R
import com.example.lipe.databinding.FragmentReportsForModeratorsBinding

class ReportsForModeratorsFragment : Fragment() {

    private lateinit var binding: FragmentReportsForModeratorsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportsForModeratorsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}