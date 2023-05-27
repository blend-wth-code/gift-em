package com.example.giftem.models

import java.util.*

data class User(val id: String = "",
                val fname: String = "",
                val lname: String = "",
                val avatar: String? = "",
                val createdAt: Long = Date().time,
                val friends: MutableList<String> = mutableListOf(),
                val approvedRequests: MutableList<String> = mutableListOf(),
                val pendingRequests: MutableList<String> = mutableListOf()
) {
    constructor() : this("", "", "", "" ,  Date().time, mutableListOf(), mutableListOf(), mutableListOf())
}