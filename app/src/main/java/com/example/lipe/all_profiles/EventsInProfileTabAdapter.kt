package com.example.lipe.all_profiles

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.lipe.all_profiles.cur_events.CurEventsInProfileFragment
import com.example.lipe.all_profiles.cur_events.YourEventsFragment
import com.example.lipe.chats_and_groups.all_chats.AllChatsFragment
import com.example.lipe.chats_and_groups.all_groups.AllGroupsFragment

class EventsInProfileTabAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, var personUid: String) : FragmentStateAdapter(fragmentManager, lifecycle){
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return if(position == 0) {
            CurEventsInProfileFragment(personUid)
        } else {
            YourEventsFragment(personUid)
        }
    }
}