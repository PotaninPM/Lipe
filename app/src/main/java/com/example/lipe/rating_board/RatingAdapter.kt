package com.example.lipe.rating_board

import android.text.TextUtils.replace
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.lipe.MainActivity
import com.example.lipe.R
import com.example.lipe.all_profiles.other_profile.OtherProfileFragment
import com.example.lipe.chats_and_groups.chat_fr.ChatFragment
import com.example.lipe.databinding.RatingItemBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList

class RatingAdapter(val lifecycleScope: LifecycleCoroutineScope) :  RecyclerView.Adapter<RatingAdapter.RatingHolder>() {

    val ratingList = ArrayList<RatingItem>()
    inner class RatingHolder(item: View): RecyclerView.ViewHolder(item) {

        private lateinit var dbRef: DatabaseReference
        private lateinit var auth: FirebaseAuth

        val binding = RatingItemBinding.bind(item)
        fun bind(rating: RatingItem) = with(binding) {
            auth = FirebaseAuth.getInstance()
            lifecycleScope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    Coil.imageLoader(itemView.context).execute(
                        ImageRequest.Builder(itemView.context)
                            .data(rating.avatarUrl)
                            .build()
                    ).drawable?.toBitmap()
                }
                binding.persImage.setImageBitmap(bitmap)
            }
            if(rating.uid != auth.currentUser!!.uid) {
                username.text = rating.username
            } else {
                username.text = "Вы(${rating.username})"
            }
            ratingScore.text = rating.score.toString()
            place.text = rating.place.toString()

            val backgroundResId = when (rating.place) {
                1 -> R.drawable.rating_item_source_first
                2 -> R.drawable.rating_item_source_second
                3 -> R.drawable.rating_item_source_third
                else -> R.drawable.rating_item_source
            }
            ratingItem.setBackgroundResource(backgroundResId)

            binding.ratingItem.setOnClickListener {
                try {
                    binding.ratingItem.isEnabled = false
                    val context = it.context
                    if (context is AppCompatActivity) {
                        if (rating.uid != auth.currentUser!!.uid) {
                            val context = it.context
                            if (context is AppCompatActivity) {
                                val fragment = OtherProfileFragment(rating.uid)
                                val fragmentManager = context.supportFragmentManager
                                fragmentManager.beginTransaction()
                                    .replace(R.id.allRating, fragment)
                                    .addToBackStack(null)
                                    .commit()
                                binding.ratingItem.isEnabled = true
                            } else {
                                binding.ratingItem.isEnabled = true
                            }
                        } else {
                            binding.ratingItem.isEnabled = true
                        }
                    } else {
                        binding.ratingItem.isEnabled = true
                    }
                } catch (e: Exception) {
                    Log.e("INFOG", "${e.message.toString()}")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rating_item, parent, false)
        return RatingHolder(view)
    }
    override fun onBindViewHolder(holder: RatingHolder, position: Int) {
        holder.bind(ratingList[position])
    }
    override fun getItemCount(): Int {
        return ratingList.size
    }
    fun filter(text: String, allRate: List<RatingItem>) {
        ratingList.clear()
        if (text.isEmpty()) {
            ratingList.addAll(allRate.sortedBy {
                it.place
            })
        } else {
            ratingList.addAll(allRate.filter {
                it.username.contains(text, ignoreCase = true)
            })
        }
        notifyDataSetChanged()
    }



    fun removeRequest(position: Int) {
        if (position in 0 until ratingList.size) {
            ratingList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateRequests(ratings: List<RatingItem>) {
        ratingList.clear()
        ratingList.addAll(ratings)
        notifyDataSetChanged()
    }
}