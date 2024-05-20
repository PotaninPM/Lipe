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
import com.example.lipe.all_profiles.cur_events.YourEventsFragment
import com.example.lipe.chats_and_groups.chat_fr.ChatFragment
import com.example.lipe.database_models.ChatModelDB
import com.example.lipe.notifications.FriendRequestData
import com.example.lipe.notifications.RetrofitInstance
import com.example.lipe.viewModels.OtherProfileVM
import com.google.android.material.tabs.TabLayout
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
import retrofit2.Call
import java.util.UUID

class OtherProfileFragment(val personUid: String) : Fragment() {

    private lateinit var binding: FragmentOtherProfileBinding

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
        binding = FragmentOtherProfileBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference()
        storageRef = FirebaseStorage.getInstance().reference

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = otherProfileVM

            loadingProgressBar.visibility = View.VISIBLE
            allProfile.visibility = View.GONE

            findAccount { userData ->
                checkIfUserAlreadyFriend { friendStatus ->
                    if (userData != null) {
                        otherProfileVM.setInfo(
                            userData.nickname,
                            userData.friendsAmount,
                            userData.eventsAmount,
                            userData.ratingPoints,
                            userData.desc,
                            userData.name,
                            friendStatus
                        )
                        loadingProgressBar.visibility = View.GONE
                        allProfile.visibility = View.VISIBLE
                    }
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

        setupTabLayout()

        binding.addToFriends.setOnClickListener {
            when(otherProfileVM.friendStatus.value.toString()) {
                "friend" -> replaceFragment(ChatFragment(""))
                "not" -> sendFriendsRequest()
                "query_to_you" -> acceptFriendRequest()
                "query_from_you" -> deleteFriendRequest()
            }
        }

    }

    private fun deleteFriendRequest() {
        val dbRef_accepter_query = FirebaseDatabase.getInstance()
            .getReference("users/${personUid}/query_friends/${auth.currentUser!!.uid}")
        dbRef_accepter_query.removeValue().addOnSuccessListener {
            binding.addToFriends.text = getString(R.string.add_to_friends)
        }
    }
    private fun acceptFriendRequest() {
        val you = auth.currentUser!!.uid
        val person = personUid
        val dbRef_accepter_friends = FirebaseDatabase.getInstance()
            .getReference("users/${you}/friends")
        val dbRef_sender_friends = FirebaseDatabase.getInstance()
            .getReference("users/${personUid}/friends")

        val dbRef_sender_chats = FirebaseDatabase.getInstance()
            .getReference("users/${personUid}/chats")
        val dbRef_accepter_chats = FirebaseDatabase.getInstance()
            .getReference("users/${you}/chats")

        val dbRef_chats = FirebaseDatabase.getInstance()
            .getReference("chats")

        val dbRef_accepter_query = FirebaseDatabase.getInstance()
            .getReference("users/${you}/query_friends")
        val dbRef_sender_query = FirebaseDatabase.getInstance()
            .getReference("users/${personUid}/query_friends")

        dbRef_accepter_friends.child(personUid).setValue(personUid)
            .addOnSuccessListener {
                dbRef_sender_friends.child(you)
                    .setValue(you).addOnSuccessListener {
                        dbRef_accepter_query.child(person).removeValue()
                            .addOnSuccessListener {
                                dbRef_sender_query.child(you).removeValue()
                                    .addOnSuccessListener {
                                        val uid_chat = UUID.randomUUID().toString()
                                        dbRef_chats.child(uid_chat).setValue(ChatModelDB(you, personUid, "", arrayListOf())).addOnSuccessListener {
                                            dbRef_sender_chats.child(uid_chat).setValue(uid_chat).addOnSuccessListener {
                                                dbRef_accepter_chats.child(uid_chat).setValue(uid_chat).addOnSuccessListener {
                                                    val request = FriendRequestData(personUid, auth.currentUser!!.uid)
                                                    val call: Call<Void> = RetrofitInstance.api.acceptFriendsRequestData(request)

                                                    binding.addToFriends.text = getString(R.string.write_to_friend)
                                                }.addOnFailureListener {
                                                    Log.d("INFOG", "ErrorRequest")
                                                }
                                            }.addOnSuccessListener {
                                                Log.d("INFOG", "ErrorRequest")
                                            }
                                        }.addOnFailureListener {
                                            Log.d("INFOG", "ErrorRequest")
                                        }

                                    }
                            }
                    }.addOnFailureListener {
                        Log.e("INFOG", "Err Request Friend")
                    }
            }.addOnFailureListener {
                Log.e("INFOG", "Err Request Friend")
            }
    }

    private fun sendFriendsRequest() {
        val dbRef = FirebaseDatabase.getInstance().getReference("users/${personUid}/query_friends")
        dbRef.child(auth.currentUser!!.uid).setValue(auth.currentUser!!.uid).addOnSuccessListener {
            val request = FriendRequestData(personUid, auth.currentUser!!.uid)
            val call: Call<Void> = RetrofitInstance.api.sendFriendsRequestData(request)
            binding.addToFriends.text = getString(R.string.request_sent)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.allOtherProfile, fragment)
        fragmentTransaction.commit()
    }
    private fun checkIfUserAlreadyFriend(status: (String) -> Unit) {
        val currentUser = auth.currentUser ?: return
        val dbRefUserFriends = FirebaseDatabase.getInstance().getReference("users/${currentUser.uid}/friends")
        val dbRefUserQueryFriendsToYou = FirebaseDatabase.getInstance().getReference("users/${currentUser.uid}/query_friends")
        val dbRefUserQueryFriendsToCreator = FirebaseDatabase.getInstance().getReference("users/${personUid}/query_friends")

        dbRefUserFriends.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (user in snapshot.children) {
                    if (user.value.toString() == personUid) {
                        if (isAdded) {
                            binding.addToFriends.text = getString(R.string.write_to_friend)
                            status("friend")
                        }
                        return
                    }
                }

                dbRefUserQueryFriendsToYou.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (user in snapshot.children) {
                            if (user.value == auth.currentUser!!.uid) {
                                if (isAdded) {
                                    binding.addToFriends.text = getString(R.string.accept_friendsheep)
                                    status("query_to_you")
                                }
                                return
                            }
                        }

                        dbRefUserQueryFriendsToCreator.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (user in snapshot.children) {
                                    if (user.value == auth.currentUser!!.uid) {
                                        if (isAdded) {
                                            binding.addToFriends.text = getString(R.string.request_sent)
                                            status("query_from_you")
                                        }
                                        return
                                    }
                                }

                                if (isAdded) {
                                    status("not")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("FirebaseError", "Error: ${error.message}")
                            }
                        })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseError", "Error: ${error.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error: ${error.message}")
            }
        })
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
    private fun setupTabLayout() {
        val tabLayout = binding.tabLayout
        tabLayout.addTab(tabLayout.newTab().setText(R.string.cur_events))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.users_events)))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> switchTabs(0)
                    1 -> switchTabs(1)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun switchTabs(position: Int) {
        val fragment = when(position) {
            0 -> CurEventsInProfileFragment(personUid)
            1 -> YourEventsFragment(personUid)
            else -> null
        }

        fragment?.let {
            childFragmentManager.beginTransaction()
                .replace(R.id.switcher_other, it)
                .commit()
        }
    }

//    private fun replaceFragment(fragment: Fragment) {
//        val fragmentManager = childFragmentManager
//        val fragmentTransaction = fragmentManager.beginTransaction()
//        fragmentTransaction.replace(R.id., fragment)
//        fragmentTransaction.addToBackStack(null)
//        fragmentTransaction.commit()
//    }

}

data class UserData(
    val nickname: String,
    val ratingPoints: Int,
    val friendsAmount: Int,
    val eventsAmount: Int,
    val desc: String,
    val name: String
)
