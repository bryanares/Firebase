package com.example.firebaseathentication.data.repository

import com.example.firebaseathentication.data.local.SpendingHistory
import com.example.firebaseathentication.data.local.User
import com.example.firebaseathentication.data.utils.Rezults
import kotlinx.coroutines.flow.Flow


interface MainRepository{

        suspend fun signIn(email: String, password: String): Flow<Rezults<User>>

        suspend fun signUp(email: String, password: String, name: String): Flow<Rezults<User>>

        //add or update spending history
        suspend fun addOrUpdateSpendingHistory(userId: String, historyId: String? = null,  history: SpendingHistory): Flow<Rezults<SpendingHistory>>

        //get all spending history
        suspend fun getAllSpendingHistory(userId: String): Flow<Rezults<List<SpendingHistory>>>

        //get one spending history
        suspend fun getSpendingHistory(userId: String, historyId: String): Flow<Rezults<SpendingHistory>>

        //delete single spending history
        suspend fun deleteSingleSpendingHistory(userId: String, historyId: String) : Flow<Rezults<List<SpendingHistory>>>
}