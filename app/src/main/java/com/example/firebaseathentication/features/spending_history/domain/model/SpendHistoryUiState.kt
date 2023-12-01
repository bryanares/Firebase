package com.example.firebaseathentication.features.spending_history.domain.model

import com.example.firebaseathentication.data.local.SpendingHistory

data class SpendHistoryUiState(
    val isLoading: Boolean = false,
    val isSuccessful: Boolean = false,
    val error: String? = null,
    val updatedSpendHistory: SpendingHistory? = null,
    val createdSpendHistory: SpendingHistory? = null,
    val singleSpendHistory: SpendingHistory? = null,
    val spendHistoryList: List<SpendingHistory>? = null
)