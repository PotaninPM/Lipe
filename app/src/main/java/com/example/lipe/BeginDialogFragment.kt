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

        if(isAdded && context != null) {
            tips = listOf(
                getString(R.string.preparation),
                getString(R.string.finger),
                getString(R.string.theme_help),
                getString(R.string.friends_view),
                getString(R.string.begin_friend_dialog),
                getString(R.string.copy),
                getString(R.string.can)
            )

            gifUrls = listOf(
                R.drawable.classroom,
                R.drawable.left_click,
                R.drawable.lipe,
                R.drawable.friends_photo,
                R.drawable.friend_dialog,
                R.drawable.event_instruct,
                R.drawable.success,
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentBeginDialogBinding.inflate(inflater, container, false)

        if(isAdded && context != null) {
            updateTip()

            binding.nextTipButton.setOnClickListener {
                currentTipIndex += 1
                updateTip()
            }

            binding.backTipButton.setOnClickListener {
                currentTipIndex -= 1
                updateTip()
            }
        }

        return binding.root
    }

    private fun updateTip() {
        if (isAdded && context != null) {
            if (currentTipIndex == tips.size) {
                dismiss()
            } else {
                binding.tipTextView.text = tips[currentTipIndex]
                binding.gifImageView.load(gifUrls[currentTipIndex])
                binding.progressBar.progress = ((currentTipIndex + 1).toFloat() / tips.size.toFloat() * 100).toInt()

                binding.backTipButton.visibility = if (currentTipIndex == 0) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    companion object {
        fun newInstance(): BeginDialogFragment {
            return BeginDialogFragment()
        }
    }
}