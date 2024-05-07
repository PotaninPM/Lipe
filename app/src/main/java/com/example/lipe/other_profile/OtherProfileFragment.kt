package com.example.lipe.other_profile

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.lipe.R
import com.example.lipe.databinding.FragmentOtherProfileBinding
import com.example.lipe.your_profile.cur_events.CurEventsInProfileFragment
import com.example.lipe.rating_board.RatingFragment
import com.example.lipe.sign_up_in.SignUpFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class OtherProfileFragment : Fragment() {

    private var _binding: FragmentOtherProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference

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

        originalBackground = binding.btnCurEvent.background
        switchTabs(0)

        binding.btnYourEvents.setBackgroundResource(0)
        binding.btnPastEvent.setBackgroundResource(0)
        binding.btnCurEvent.background = originalBackground

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchTabs(0)

    }

    private fun switchTabs(position: Int) {
        val fragment = when(position) {
            0 -> CurEventsInProfileFragment()
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

    data class UserData(
        val firstName: String,
        val lastName: String,
        val ratingPoints: Int,
        val friendsAmount: Int,
        val eventsAmount: Int,
    )

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}