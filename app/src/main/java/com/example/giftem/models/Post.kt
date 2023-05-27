package com.example.giftem.models

import java.util.*

data class Post(
    val prodId: String,
    val prodName: String,
    val price: Double,
    val contributionReceived: Double,
    val prodImg: String,
    val desc: String,
    val createdBy: String,
    val createdAt: Long = Date().time
) {
    constructor() : this("", "", 0.0, 0.0, "", "", "", 0L)
}
