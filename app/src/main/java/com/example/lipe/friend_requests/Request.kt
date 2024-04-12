package com.example.lipe.friend_requests

import java.net.URL

data class Request(val avatarUrl: String, val username: String, val uid_sender: String, val uid_accepter: String)