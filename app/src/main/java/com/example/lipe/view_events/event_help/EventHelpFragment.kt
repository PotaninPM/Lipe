package com.example.lipe.view_events.event_help

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.lipe.R
import com.example.lipe.databinding.FragmentEventHelpBinding

class EventHelpFragment : Fragment() {

    private lateinit var binding: FragmentEventHelpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventHelpBinding.inflate(inflater, container, false)

        val view = binding.root
        return view
    }

}