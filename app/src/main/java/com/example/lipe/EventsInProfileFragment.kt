package com.example.lipe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.lipe.databinding.FragmentEventsInProfileBinding
import com.example.lipe.databinding.FragmentOtherProfileBinding

class EventsInProfileFragment : Fragment() {

    private var _binding: FragmentEventsInProfileBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventsInProfileBinding.inflate(inflater, container, false)

        val view = binding.root
        return view
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}