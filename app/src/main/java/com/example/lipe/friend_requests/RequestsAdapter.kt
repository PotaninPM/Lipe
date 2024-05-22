package com.example.lipe.friend_requests

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.database_models.ChatModelDB
import com.example.lipe.databinding.FriendRequestItemBinding
import com.example.lipe.notifications.FriendRequestData
import com.example.lipe.notifications.RetrofitInstance
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList
import java.util.UUID

class RequestsAdapter(val lifecycleScope: LifecycleCoroutineScope): RecyclerView.Adapter<RequestsAdapter.RequestsHolder>() {

    val requestList = ArrayList<Request>()
    inner class RequestsHolder(item: View): RecyclerView.ViewHolder(item) {
        private lateinit var dbRef: DatabaseReference

        val binding = FriendRequestItemBinding.bind(item)
        fun bind(request: Request) = with(binding) {

            lifecycleScope.launch {
                lifecycleScope.launch {
                    val bitmap: Bitmap = withContext(Dispatchers.IO) {
                        Coil.imageLoader(itemView.context).execute(
                            ImageRequest.Builder(itemView.context)
                                .data(request.avatarUrl)
                                .build()
                        ).drawable?.toBitmap()!!
                    }
                    binding.avatarRequest.setImageBitmap(bitmap)
                }
            }

            username.text = request.username

            acceptBtn.setOnClickListener {
                binding.acceptBtn.isClickable = false
                binding.declineBtn.isClickable = false

                val dbRef_accepter_friends = FirebaseDatabase.getInstance()
                    .getReference("users/${request.uid_accepter}/friends")
                val dbRef_sender_friends = FirebaseDatabase.getInstance()
                    .getReference("users/${request.uid_sender}/friends")

                val dbRef_sender_chats = FirebaseDatabase.getInstance()
                    .getReference("users/${request.uid_sender}/chats")

                val dbRef_chats = FirebaseDatabase.getInstance()
                    .getReference("chats")

                val dbRef_accepter_chats = FirebaseDatabase.getInstance()
                    .getReference("users/${request.uid_accepter}/chats")

                val dbRef_accepter_query = FirebaseDatabase.getInstance()
                    .getReference("users/${request.uid_accepter}/query_friends")
                val dbRef_sender_query = FirebaseDatabase.getInstance()
                    .getReference("users/${request.uid_sender}/query_friends")
                dbRef_accepter_friends.child(request.uid_sender).setValue(request.uid_sender)
                    .addOnSuccessListener {
                        dbRef_sender_friends.child(request.uid_accepter)
                            .setValue(request.uid_accepter).addOnSuccessListener {
                            dbRef_accepter_query.child(request.uid_sender).removeValue()
                                .addOnSuccessListener {
                                    dbRef_sender_query.child(request.uid_accepter).removeValue()
                                        .addOnSuccessListener {
                                            val uid_chat = UUID.randomUUID().toString()
                                            dbRef_chats.child(uid_chat).setValue(ChatModelDB(request.uid_accepter, request.uid_sender)).addOnSuccessListener {
                                                dbRef_sender_chats.child(uid_chat).setValue(uid_chat).addOnSuccessListener {
                                                    dbRef_accepter_chats.child(uid_chat).setValue(uid_chat).addOnSuccessListener {
                                                        if (adapterPosition != RecyclerView.NO_POSITION) {
                                                            val request = FriendRequestData(request.uid_accepter, request.uid_sender)
                                                            val call: Call<Void> = RetrofitInstance.api.acceptFriendsRequestData(request)

                                                            Log.d("INFOG", call.toString())

                                                            call.enqueue(object : Callback<Void> {
                                                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                                                    if (response.isSuccessful) {
                                                                        Log.d("INFOG", "notification was sent")
                                                                    } else {
                                                                        Log.d("INFOG", "${response.message()}")
                                                                    }
                                                                }

                                                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                                                    Log.d("INFOG", "${t.message}")
                                                                }
                                                            })
                                                            removeRequest(adapterPosition)
                                                        }
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

            declineBtn.setOnClickListener {
                binding.acceptBtn.isClickable = false
                binding.declineBtn.isClickable = false
                val dbRef_accepter_query = FirebaseDatabase.getInstance()
                    .getReference("users/${request.uid_accepter}/query_friends")
                val dbRef_sender_query = FirebaseDatabase.getInstance()
                    .getReference("users/${request.uid_sender}/query_friends")
                dbRef_accepter_query.child(request.uid_sender).removeValue().addOnSuccessListener {
                    dbRef_sender_query.child(request.uid_accepter).removeValue()
                        .addOnSuccessListener {
                            if (adapterPosition != RecyclerView.NO_POSITION) {
                                removeRequest(adapterPosition)
                            }
                        }
                }.addOnFailureListener {
                    Log.e("INFOG", "Err Request Friend")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_request_item, parent, false)
        return RequestsHolder(view)
    }
    override fun onBindViewHolder(holder: RequestsHolder, position: Int) {
        holder.bind(requestList[position])
    }
    override fun getItemCount(): Int {
        return requestList.size
    }

    fun removeRequest(position: Int) {
        if (position in 0 until requestList.size) {
            requestList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateRequests(requests: List<Request>) {
        requestList.clear()
        requestList.addAll(requests)
        notifyDataSetChanged()
    }


}
