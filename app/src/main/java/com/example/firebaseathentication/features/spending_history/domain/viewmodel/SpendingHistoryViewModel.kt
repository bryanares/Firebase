package com.example.firebaseathentication.features.spending_history.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseathentication.data.local.SpendingHistory
import com.example.firebaseathentication.data.repository.MainRepository
import com.example.firebaseathentication.data.utils.Rezults
import com.example.firebaseathentication.features.spending_history.domain.model.SpendHistoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpendingHistoryViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {
    private val _spendingHistoryUiState = MutableStateFlow(SpendHistoryUiState())
    val spendingHistoryUiState = _spendingHistoryUiState.asStateFlow()

    fun resetState() {
        _spendingHistoryUiState.update { SpendHistoryUiState() }
    }


    fun addSpendingHistory(userId: String, date: Long, amount: Float, description: String) {
        viewModelScope.launch(Dispatchers.IO) {

            repository.addOrUpdateSpendingHistory(
                userId,
                null,
                SpendingHistory(date = date, amount = amount, description = description)
            ).collectLatest { result ->
                when (result) {
                    is Rezults.Success -> {
                        _spendingHistoryUiState.update {
                            SpendHistoryUiState(
                                isLoading = false,
                                isSuccessful = true,
                                createdSpendHistory = result.data
                            )
                        }
                    }

                    is Rezults.Error -> {
                        _spendingHistoryUiState.update {
                            SpendHistoryUiState(
                                isLoading = false,
                                isSuccessful = false,
                                error = result.exception?.message
                            )
                        }
                    }
                }
            }
        }
    }


    fun updateSpendingHistory(
        userId: String,
        historyId: String,
        date: Long? = null,
        amount: Float? = null,
        description: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val oldSpending = _spendingHistoryUiState.value.singleSpendHistory ?: return@launch
            val newSpending = oldSpending.copy(
                date = date ?: oldSpending.date,
                amount = amount ?: oldSpending.amount,
                description = description ?: oldSpending.description,
            )

            repository.addOrUpdateSpendingHistory(
                userId,
                historyId,
                newSpending
            )
                .collectLatest { result ->
                    when (result) {
                        is Rezults.Success -> {
                            _spendingHistoryUiState.update {
                                SpendHistoryUiState(
                                    isLoading = false,
                                    isSuccessful = true,
                                    updatedSpendHistory = result.data
                                )
                            }
                        }

                        is Rezults.Error -> {
                            _spendingHistoryUiState.update {
                                SpendHistoryUiState(
                                    isLoading = false,
                                    isSuccessful = false,
                                    error = result.exception?.message
                                )
                            }
                        }
                    }
                }
        }
    }

    fun getSingleSpendingHistory(userId: String, historyId: String) {
        viewModelScope.launch(Dispatchers.IO) {

            repository.getSpendingHistory(userId, historyId).collectLatest { result ->
                when (result) {
                    is Rezults.Success -> {
                        _spendingHistoryUiState.update {
                            SpendHistoryUiState(
                                isLoading = false,
                                isSuccessful = true,
                                singleSpendHistory = result.data
                            )
                        }
                    }

                    is Rezults.Error -> {
                        _spendingHistoryUiState.update {
                            SpendHistoryUiState(
                                isLoading = false,
                                isSuccessful = false,
                                error = result.exception?.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun getAllSpendingHistory(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {

            repository.getAllSpendingHistory(userId).collectLatest { result ->
                when (result) {
                    is Rezults.Success -> {
                        _spendingHistoryUiState.update {
                            SpendHistoryUiState(
                                isLoading = false,
                                isSuccessful = true,
                                spendHistoryList = result.data
                            )
                        }
                    }

                    is Rezults.Error -> {
                        _spendingHistoryUiState.update {
                            SpendHistoryUiState(
                                isLoading = false,
                                isSuccessful = false,
                                error = result.exception?.message
                            )
                        }
                    }
                }
            }
        }
    }
}