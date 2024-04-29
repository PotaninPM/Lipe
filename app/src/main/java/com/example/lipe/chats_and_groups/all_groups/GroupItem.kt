package com.example.lipe.chats_and_groups.all_groups

data class GroupItem(val avatarUrl: String, val uid: String, val name: String, val last_message: String, val membersUid: ArrayList<String>)
