package com.example.lipe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.lipe.databinding.FragmentCurEventsInProfileBinding

class CurEventsInProfileFragment : Fragment() {

    private var _binding: FragmentCurEventsInProfileBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCurEventsInProfileBinding.inflate(inflater, container, false)

        val view = binding.root
        return view
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}