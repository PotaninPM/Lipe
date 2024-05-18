package com.example.lipe.all_profiles.other_profile

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.databinding.FragmentOtherProfileBinding
import com.example.lipe.all_profiles.cur_events.CurEventsInProfileFragment
import com.example.lipe.rating_board.RatingFragment
import com.example.lipe.sign_up_in.SignUpFragment
import com.example.lipe.viewModels.OtherProfileVM
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OtherProfileFragment(val personUid: String) : Fragment() {

    private var _binding: FragmentOtherProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference

    private val otherProfileVM: OtherProfileVM by activityViewModels()

    private var originalBackground: Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOtherProfileBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference()
        storageRef = FirebaseStorage.getInstance().reference

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = otherProfileVM

            loadingProgressBar.visibility = View.VISIBLE
            allProfile.visibility = View.GONE

            findAccount { userData ->
                if(userData != null) {
                    otherProfileVM.setInfo(
                        userData.nickname,
                        userData.friendsAmount,
                        userData.eventsAmount,
                        userData.ratingPoints,
                        userData.desc,
                        userData.name
                    )
                    loadingProgressBar.visibility = View.GONE
                    allProfile.visibility = View.VISIBLE
                }
            }

            setProfilePhotos {
                if(it) {
                    loadingProgressBar.visibility = View.GONE
                    allProfile.visibility = View.VISIBLE
                }
            }
        }

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchTabs(0)

    }
    private fun setProfilePhotos(callback: (Boolean) -> Unit) {
        val photoRef = storageRef.child("avatars/${personUid}")
        val themeRef = storageRef.child("user_theme/${personUid}")

        val tokenTaskAvatar = photoRef.downloadUrl
        val tokenTaskTheme = themeRef.downloadUrl

        tokenTaskAvatar.addOnSuccessListener { url_avatar ->
            lifecycleScope.launch {
                val bitmap: Bitmap = withContext(Dispatchers.IO) {
                    Coil.imageLoader(requireContext()).execute(
                        ImageRequest.Builder(requireContext())
                            .data(url_avatar)
                            .build()
                    ).drawable?.toBitmap()!!
                }
                binding.avatar.setImageBitmap(bitmap)
            }
            tokenTaskTheme.addOnSuccessListener { url_theme ->
                lifecycleScope.launch {
                    val bitmap: Bitmap = withContext(Dispatchers.IO) {
                        Coil.imageLoader(requireContext()).execute(
                            ImageRequest.Builder(requireContext())
                                .data(url_theme)
                                .build()
                        ).drawable?.toBitmap()!!
                    }
                    binding.theme.setImageBitmap(bitmap)
                }
                callback(true)
            }
        }.addOnFailureListener {
            callback(false)
        }
    }
    private fun findAccount(callback: (UserData?) -> Unit) {
        try {
            val dbRef_user =
                FirebaseDatabase.getInstance().getReference("users/${personUid}")
            dbRef_user.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val desc = dataSnapshot.child("about_you").value.toString()
                    val name = dataSnapshot.child("firstAndLastName").value.toString()
                    val username: String = dataSnapshot.child("username").value.toString()
                    val ratingAmount: Int = dataSnapshot.child("points").value.toString().toInt()
                    val friendsAmount: Int = dataSnapshot.child("friends_amount").value.toString().toInt()
                    val eventsAmount: Int = dataSnapshot.child("events_amount").value.toString().toInt()

                    callback(
                        UserData(
                            username,
                            ratingAmount,
                            friendsAmount,
                            eventsAmount,
                            desc,
                            name
                        )
                    )
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("FirebaseError", "Ошибка Firebase ${databaseError.message}")
                    callback(null)
                }
            })
        } catch (e: Exception) {
            Log.e("INFOG", e.message.toString())
        }
    }

    private fun switchTabs(position: Int) {
        val fragment = when(position) {
            0 -> CurEventsInProfileFragment(auth.currentUser!!.uid)
            1 -> RatingFragment()
            2 -> SignUpFragment()
            else -> null
        }

        fragment?.let {
            childFragmentManager.beginTransaction()
                .replace(R.id.switcher, it)
                .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}

data class UserData(
    val nickname: String,
    val ratingPoints: Int,
    val friendsAmount: Int,
    val eventsAmount: Int,
    val desc: String,
    val name: String
)
