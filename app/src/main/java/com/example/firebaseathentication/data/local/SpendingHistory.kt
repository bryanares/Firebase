package com.example.firebaseathentication.data.local

data class SpendingHistory (
    val id: String? = null,
    val userId: String? = null,
    val date: Long? = null,
    val amount: Float? = null,
    val category: String ?= null,
    val description: String? = null,
    var createdTime: Long? = null,
    var updatedTime: Long? = null,
)