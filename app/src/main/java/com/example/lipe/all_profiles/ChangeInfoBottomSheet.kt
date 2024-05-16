package com.example.lipe.all_profiles

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.activityViewModels
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.databinding.BottomSheetLayoutBinding
import com.example.lipe.viewModels.ProfileVM
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChangeInfoBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetLayoutBinding? = null
    private val binding get() = _binding!!

    private val profileVM: ProfileVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = profileVM

//            val bitmap: Bitmap = withContext(Dispatchers.IO) {
//                Coil.imageLoader(requireContext()).execute(
//                    ImageRequest.Builder(requireContext())
//                        .data()
//                        .build()
//                ).drawable?.toBitmap()!!
//            }
//            binding.avatar.setImageBitmap(bitmap)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
//        dialog.setOnShowListener {
//            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//            bottomSheet?.layoutParams?.height = (resources.displayMetrics.heightPixels * 0.6).toInt()
//        }
        return dialog
    }
}