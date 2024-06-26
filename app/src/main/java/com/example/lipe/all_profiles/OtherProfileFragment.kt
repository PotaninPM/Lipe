package com.example.lipe.all_profiles.other_profile

import android.graphics.Bitmap
import android.graphics.Color
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
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.all_profiles.EventsInProfileTabAdapter
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
import com.google.firebase.database.collection.LLRBNode
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class OtherProfileFragment(val personUid: String) : Fragment() {

    private lateinit var binding: FragmentOtherProfileBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference

    private lateinit var adapter: EventsInProfileTabAdapter

    private val otherProfileVM: OtherProfileVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOtherProfileBinding.inflate(inflater, container, false)

        if(isAdded) {
            auth = FirebaseAuth.getInstance()
            dbRef = FirebaseDatabase.getInstance().getReference()
            storageRef = FirebaseStorage.getInstance().reference

            binding.apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = otherProfileVM

                adapter = EventsInProfileTabAdapter(childFragmentManager, lifecycle, personUid)
                binding.switcherOther.adapter = adapter

                loadingProgressBar.visibility = View.VISIBLE
                allProfile.visibility = View.GONE

                findAccount { userData ->
                    checkIfUserAlreadyFriend { friendStatus ->
                        Log.d("INFOG", friendStatus)
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
                    if (it) {
                        loadingProgressBar.visibility = View.GONE
                        allProfile.visibility = View.VISIBLE
                    }
                }
            }
        }

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            tabLayout.apply {

                addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        binding.switcherOther.currentItem = tab!!.position
                    }

                    override fun onTabUnselected(p0: TabLayout.Tab?) {}

                    override fun onTabReselected(p0: TabLayout.Tab?) {}

                })
            }

            switcherOther.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
                }
            })
        }

        binding.addToFriends.setOnClickListener {
            sendFriendsRequest()
        }

        binding.delete.setOnClickListener {
            deleteFromFriends()
        }

        binding.request.setOnClickListener {
            deleteFriendRequest()
        }

        binding.accept.setOnClickListener {
            acceptFriendRequest()
        }

    }

    private fun deleteFromFriends() {
        if(isAdded) {
            binding.delete.isEnabled = false
            val yourFriends = FirebaseDatabase.getInstance()
                .getReference("users/${auth.currentUser!!.uid}/friends/${personUid}")
            val friend = FirebaseDatabase.getInstance()
                .getReference("users/${personUid}/friends/${auth.currentUser!!.uid}")

            val chatRefPers = FirebaseDatabase.getInstance()
                .getReference("users/${personUid}/chats/${auth.currentUser!!.uid}")

            val chatRefYour = FirebaseDatabase.getInstance()
                .getReference("users/${auth.currentUser!!.uid}/chats/${personUid}")

            chatRefPers.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        val chat = FirebaseDatabase.getInstance().getReference("chats/${snapshot.value}")
                        chat.removeValue().addOnSuccessListener {
                            chatRefPers.removeValue().addOnSuccessListener {
                                chatRefYour.removeValue().addOnSuccessListener {

                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

            yourFriends.removeValue().addOnSuccessListener {
                friend.removeValue().addOnSuccessListener {
                    binding.delete.visibility = View.INVISIBLE
                    binding.addToFriends.visibility = View.VISIBLE
                    binding.request.visibility = View.INVISIBLE
                    binding.accept.visibility= View.INVISIBLE

                    binding.delete.isEnabled = true
                }
            }
        }
    }

    private fun deleteFriendRequest() {
        binding.request.isEnabled = false

        val dbRef_accepter_query = FirebaseDatabase.getInstance()
            .getReference("users/${personUid}/query_friends/${auth.currentUser!!.uid}")
        dbRef_accepter_query.removeValue().addOnSuccessListener {
            binding.delete.visibility = View.INVISIBLE
            binding.addToFriends.visibility = View.VISIBLE
            binding.request.visibility = View.INVISIBLE
            binding.accept.visibility= View.INVISIBLE

            binding.request.isEnabled = true
        }
    }
    private fun acceptFriendRequest() {
        binding.accept.isEnabled = false

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
                                            dbRef_sender_chats.child(you).setValue(uid_chat).addOnSuccessListener {
                                                dbRef_accepter_chats.child(person).setValue(uid_chat).addOnSuccessListener {
                                                    val request = FriendRequestData(personUid, auth.currentUser!!.uid)
                                                    val call: Call<Void> = RetrofitInstance.api.acceptFriendsRequestData(request)
                                                    binding.accept.isEnabled = true

                                                    binding.delete.visibility = View.VISIBLE
                                                    binding.addToFriends.visibility = View.INVISIBLE
                                                    binding.request.visibility = View.INVISIBLE
                                                    binding.accept.visibility= View.INVISIBLE
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
        binding.addToFriends.isEnabled = false
        val dbRef = FirebaseDatabase.getInstance().getReference("users/${personUid}/query_friends")
        dbRef.child(auth.currentUser!!.uid).setValue(auth.currentUser!!.uid).addOnSuccessListener {

            val request = FriendRequestData(personUid, auth.currentUser!!.uid)
            val call: Call<Void> = RetrofitInstance.api.sendFriendsRequestData(request)

            Log.d("INFOG", call.toString())

            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        binding.addToFriends.isEnabled = true

                        binding.delete.visibility = View.INVISIBLE
                        binding.addToFriends.visibility = View.INVISIBLE
                        binding.request.visibility = View.VISIBLE
                        binding.accept.visibility= View.INVISIBLE
                    } else {
                        Log.d("INFOG", "${response.message()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.d("INFOG", "${t.message}")
                }
            })
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
                            binding.delete.visibility = View.VISIBLE
                            binding.addToFriends.visibility = View.INVISIBLE
                            binding.request.visibility = View.INVISIBLE
                            binding.accept.visibility= View.INVISIBLE
                            status("friend")
                        }
                        return
                    }
                }

                dbRefUserQueryFriendsToYou.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (user in snapshot.children) {
                            if (user.value == personUid) {
                                if (isAdded) {
                                    binding.addToFriends.visibility = View.INVISIBLE
                                    binding.request.visibility = View.INVISIBLE
                                    binding.accept.visibility= View.VISIBLE
                                    binding.delete.visibility = View.INVISIBLE
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
                                            binding.addToFriends.visibility = View.INVISIBLE
                                            binding.request.visibility = View.VISIBLE
                                            binding.accept.visibility= View.INVISIBLE
                                            binding.delete.visibility = View.INVISIBLE
                                            status("query_from_you")
                                        }
                                        return
                                    }
                                }

                                if (isAdded) {
                                    status("not")

                                    binding.addToFriends.visibility = View.VISIBLE
                                    binding.request.visibility = View.INVISIBLE
                                    binding.accept.visibility= View.INVISIBLE
                                    binding.delete.visibility = View.INVISIBLE
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
                    val friendsAmount: Int = dataSnapshot.child("friends").childrenCount.toInt() ?: 0
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

}

data class UserData(
    val nickname: String,
    val ratingPoints: Int,
    val friendsAmount: Int,
    val eventsAmount: Int,
    val desc: String,
    val name: String
)
