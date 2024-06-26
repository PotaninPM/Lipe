package com.example.lipe.all_profiles.change_info_sheet

import android.app.Dialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.databinding.BottomSheetChangeYourInfoLayoutBinding
import com.example.lipe.viewModels.ProfileVM
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.concurrent.CountDownLatch

class ChangeInfoBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetChangeYourInfoLayoutBinding

    private val profileVM: ProfileVM by activityViewModels()

    private lateinit var avatarUri: Uri

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetChangeYourInfoLayoutBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = profileVM

            binding.saving.visibility = View.GONE
            binding.statusSave.visibility = View.GONE

            lifecycleScope.launch {
                val bitmap: Bitmap = withContext(Dispatchers.IO) {
                    Coil.imageLoader(requireContext()).execute(
                        ImageRequest.Builder(requireContext())
                            .data(profileVM.avatar.value)
                            .build()
                    ).drawable?.toBitmap()!!
                }
                binding.avatar.setImageBitmap(bitmap)
            }

            binding.saveAllChanges.setOnClickListener {
                allEditProfile.visibility = View.GONE
                saving.visibility = View.VISIBLE
                statusSave.visibility = View.VISIBLE

                GlobalScope.launch {
                    updateUserData { ans ->
                        if(ans == "ok") {
                            dismiss()
                        } else {
                            binding.saving.visibility = View.GONE
                            binding.statusSave.visibility = View.GONE
                            allEditProfile.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun updateUserData(ans: (String) -> Unit) {
        val dbRef_user = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}")

        val newNick = binding.etLogininput.text.toString()
        val newName = binding.etNameAndSurnameinput.text.toString()
        val newDesc = binding.descText.text.toString()

        val latch = CountDownLatch(3)

        if(newNick != profileVM.nickname.value.toString() && newNick.isNotEmpty()) {
            dbRef_user.child("username").setValue(newNick).addOnSuccessListener {
                latch.countDown()
            }.addOnFailureListener {
                latch.countDown()
            }
        } else {
            latch.countDown()
        }

        if(newName != profileVM.name.value.toString() && newName.isNotEmpty()) {
            dbRef_user.child("firstAndLastName").setValue(newName).addOnSuccessListener {
                latch.countDown()
            }.addOnFailureListener {
                latch.countDown()
            }
        } else {
            latch.countDown()
        }

        if(newDesc != profileVM.desc.value.toString() && newDesc.isNotEmpty()) {
            dbRef_user.child("about_you").setValue(newDesc).addOnSuccessListener {
                latch.countDown()
            }.addOnFailureListener {
                latch.countDown()
            }
        } else {
            latch.countDown()
        }

        latch.await()

        ans("ok")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        return dialog
    }
}