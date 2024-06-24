package com.example.lipe

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import coil.load
import com.example.lipe.databinding.FragmentBeginDialogBinding

class BeginDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentBeginDialogBinding

    private var currentTipIndex = 0
    private var progress = 0

    private lateinit var tips: List<String>
    private lateinit var gifUrls: List<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tips = listOf(
            getString(R.string.preparation),
            getString(R.string.finger),
            getString(R.string.theme_help),
            getString(R.string.can)
        )

        gifUrls = listOf(
            R.drawable.classroom,
            R.drawable.left_click,
            R.drawable.lipe,
            R.drawable.success,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentBeginDialogBinding.inflate(inflater, container, false)

        updateTip()

        binding.nextTipButton.setOnClickListener {
            currentTipIndex += 1
            updateTip()
        }

        return binding.root
    }

    private fun updateTip() {
        if (currentTipIndex == tips.size) {
            dismiss()
        } else {
            progress = (currentTipIndex + 1) * 25
            binding.progressText.text = "$progress%"
            binding.progressBar.progress = progress
            binding.tipTextView.text = tips[currentTipIndex]
            binding.gifImageView.load(gifUrls[currentTipIndex])
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    companion object {
        fun newInstance(): BeginDialogFragment {
            return BeginDialogFragment()
        }
    }
}