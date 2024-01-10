package com.example.firebaseathentication.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.firebaseathentication.data.local.SpendingHistory
import com.example.firebaseathentication.data.local.User
import com.example.firebaseathentication.data.utils.FirebaseDocument
import com.example.firebaseathentication.data.utils.Rezults
import com.example.firebaseathentication.data.utils.toLong
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.LocalDateTime
import javax.inject.Inject

const val TAG : String = "Repo"
class MainRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val myfirebaseFirestore: FirebaseFirestore
) : MainRepository
{
    override suspend fun signIn(email: String, password: String): Flow<Rezults<User>> {
        return callbackFlow {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        this.trySend(
                            Rezults.Success(
                                User(
                                    firebaseAuth.currentUser!!.uid
                                )
                            )
                        ).isSuccess


                    } else {
                        this.trySend(
                            Rezults.Error(
                                "",
                                task.exception!!
                            )
                        ).isSuccess
                    }
                }
            awaitClose { this.cancel() }
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        name: String
    ): Flow<Rezults<User>> = callbackFlow {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        this.trySend(
                            Rezults.Success(
                                User(
                                    firebaseAuth.currentUser!!.uid
                                )
                            )
                        ).isSuccess

                    } else {
                        this.trySend(
                            Rezults.Error(
                                "",
                                task.exception!!
                            )
                        ).isSuccess
                    }
                }
            awaitClose { this.cancel() }
    }


    //add or update spending history
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun addOrUpdateSpendingHistory(
        userId: String,
        historyId: String?,
        history: SpendingHistory
    ): Flow<Rezults<SpendingHistory>> = callbackFlow {

        //create unique id for spending history
        val fileHistoryId = historyId ?: createUniqueID(
            userId,
            LocalDateTime.now().toLong(), "spending_history"
        )
        history.updatedTime = LocalDateTime.now().toLong()
        if (historyId == null)
            history.createdTime = history.updatedTime

        //add or update single spending history to firebase
        myfirebaseFirestore.collection(userId).document(FirebaseDocument.SPENDING_HISTORY)
            .collection(FirebaseDocument.SPENDING_HISTORY)
            .document(fileHistoryId).set(history)
            .addOnSuccessListener { documentReference ->
                var newSpendHistory = history.copy(id = fileHistoryId)

                this.trySend(
                    Rezults.Success(
                        newSpendHistory
                    )
                ).isSuccess

            }
            .addOnFailureListener { e ->
                this.trySend(
                    Rezults.Error(
                        "",
                        e
                    )
                ).isSuccess
            }

        awaitClose { this.cancel() }
    }

    override suspend fun getAllSpendingHistory(userId: String): Flow<Rezults<List<SpendingHistory>>>  = callbackFlow{
       //get all items in the collection
        myfirebaseFirestore.collection(userId).document(FirebaseDocument.SPENDING_HISTORY)
            .collection(FirebaseDocument.SPENDING_HISTORY).get()
            .addOnSuccessListener { documentReference ->
                //create instance of history list
                var historyList = mutableListOf<SpendingHistory>()
                //loop through documents in the collection, get their id's and add them to the history list
                for (document in documentReference.documents) {
                    var history = document.toObject(SpendingHistory::class.java)?.copy(
                        id = document.id
                    )
                    historyList.add(history!!)
                }
                this.trySend(
                    Rezults.Success(
                        historyList
                    )
                ).isSuccess
            }
        awaitClose { this.cancel()}
    }

    override suspend fun getSpendingHistory(
        userId: String,
        historyId: String
    ): Flow<Rezults<SpendingHistory>>  = callbackFlow{
        myfirebaseFirestore.collection(userId).document(FirebaseDocument.SPENDING_HISTORY)
            .collection(FirebaseDocument.SPENDING_HISTORY)
            .document(historyId).get()
            .addOnSuccessListener { documentReference ->
                var history = documentReference.toObject(SpendingHistory::class.java)?.copy(
                    id = documentReference.id
                )
                this.trySend(
                    Rezults.Success(
                        history!!
                    )
                ).isSuccess
            }
            .addOnFailureListener {e ->
                this.trySend(
                    Rezults.Error(
                        "",
                        e
                    )
                ).isSuccess
            }
        awaitClose { this.cancel() }
    }

    override suspend fun deleteSingleSpendingHistory(
        userId: String,
        historyId: String
    ): Flow<Rezults<List<SpendingHistory>>> = callbackFlow{
        myfirebaseFirestore.collection(userId).document(FirebaseDocument.SPENDING_HISTORY)
            .collection(FirebaseDocument.SPENDING_HISTORY)
            .document(historyId)
            .delete()
            //I want to fetch a list of the new items in the collection after deleting
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
    }

    //create unique Id using userId, timesStamp, key and key2 variables
    private fun createUniqueID(
        userId: String,
        timeStamp: Long,
        key: String,
        key2: String? = null
    ): String {
        return if (key2 != null) {
            "$userId-$timeStamp-$key-$key2"
        } else {
            "$userId-$timeStamp-$key"
        }
    }


}