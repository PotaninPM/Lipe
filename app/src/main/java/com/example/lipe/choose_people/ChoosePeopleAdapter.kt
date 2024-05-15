package com.example.lipe.choose_people

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.databinding.PersonGoItemBinding
import com.example.lipe.people_go_to_event.PersoneGoItem
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class ChoosePeopleAdapter(val lifecycleScope: LifecycleCoroutineScope) :  RecyclerView.Adapter<ChoosePeopleAdapter.ChoosePeopleHolder>() {

    val peopleGoList = ArrayList<PersoneGoItem>()

    private val selectedPeople = HashSet<String>()
    inner class ChoosePeopleHolder(item: View): RecyclerView.ViewHolder(item) {

        private lateinit var dbRef: DatabaseReference

        val binding = PersonGoItemBinding.bind(item)
        fun bind(person: PersoneGoItem) = with(binding) {
            lifecycleScope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    Coil.imageLoader(itemView.context).execute(
                        ImageRequest.Builder(itemView.context)
                            .data(person.image)
                            .build()
                    ).drawable?.toBitmap()
                }
                binding.persImage.setImageBitmap(bitmap)
            }
            binding.checkBoxPerson.visibility = View.VISIBLE
            binding.checkBoxPerson.isChecked = selectedPeople.contains(person.uid)
            username.text = person.username

            itemView.setOnClickListener {
                if(binding.checkBoxPerson.isChecked) {
                    binding.checkBoxPerson.isChecked = false
                    selectedPeople.remove(person.uid)
                } else {
                    binding.checkBoxPerson.isChecked = true
                    selectedPeople.add(person.uid)
                }
            }

            binding.checkBoxPerson.setOnClickListener {
                if(binding.checkBoxPerson.isChecked) {
                    selectedPeople.add(person.uid)
                } else {
                    selectedPeople.remove(person.uid)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoosePeopleHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.person_go_item, parent, false)
        return ChoosePeopleHolder(view)
    }
    override fun onBindViewHolder(holder: ChoosePeopleHolder, position: Int) {
        holder.bind(peopleGoList[position])
    }
    override fun getItemCount(): Int {
        return peopleGoList.size
    }

    fun getSelectedPeople(): List<String> = selectedPeople.toList()
    fun removeRequest(position: Int) {
        if (position in 0 until peopleGoList.size) {
            peopleGoList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateRequests(people: List<PersoneGoItem>) {
        peopleGoList.clear()
        peopleGoList.addAll(people)
        notifyDataSetChanged()
    }
}