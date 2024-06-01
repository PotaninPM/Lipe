package com.example.lipe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.lipe.databinding.FragmentFriendOnMapBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FriendOnMapBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentFriendOnMapBottomSheetBinding
    private lateinit var
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendOnMapBottomSheetBinding.inflate(inflater, container, false)

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}