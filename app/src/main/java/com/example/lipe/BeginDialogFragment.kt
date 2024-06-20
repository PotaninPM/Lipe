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

    private val tips = listOf(
        getString(R.string.preparation),
        "Чтобы создать событие просто удерживайте палец на экране в течении 1.5 секунд",
        "",
        "Отлично, теперь вы можете приступать к использованию приложения, удачи!)"
    )

    private val gifUrls = listOf(
        R.drawable.classroom,
        "url_to_gif_1",
        "url_to_gif_2",
        R.drawable.success,
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentBeginDialogBinding.inflate(inflater, container, false)

        updateTip()

        binding.nextTipButton.setOnClickListener {
            currentTipIndex+=1
            updateTip()
        }

        return binding.root
    }

    private fun updateTip() {

        if(currentTipIndex == 4) {
            dismiss()
        } else {
            progress += 25
            binding.progressText.setText("$progress%")
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