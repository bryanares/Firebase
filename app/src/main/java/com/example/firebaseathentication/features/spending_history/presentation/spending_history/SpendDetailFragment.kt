package com.example.firebaseathentication.features.spending_history.presentation.spending_history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.firebaseathentication.data.utils.getFullDateFromLong
import com.example.firebaseathentication.databinding.FragmentSpendDetailBinding
import com.example.firebaseathentication.features.spending_history.domain.viewmodel.SpendingHistoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SpendDetailFragment : Fragment() {
    private lateinit var spendDetailBinding: FragmentSpendDetailBinding
    private val spendingHistoryDetailFragmentArgs: SpendDetailFragmentArgs by navArgs()
    private val spendingHistoryViewModel: SpendingHistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        spendDetailBinding = FragmentSpendDetailBinding.inflate(inflater, container, false)
        return spendDetailBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectLatestStates()
        spendingHistoryViewModel.getSingleSpendingHistory(
            spendingHistoryDetailFragmentArgs.userId,
            spendingHistoryDetailFragmentArgs.recordId
        )
        spendDetailBinding.updateSleepDetail.setOnClickListener {
            findNavController().navigate(
                SpendDetailFragmentDirections.actionSpendDetailFragmentToEditSpendingFragment(
                    spendingHistoryDetailFragmentArgs.userId,
                    spendingHistoryDetailFragmentArgs.recordId
                )
            )
        }
        spendDetailBinding.deleteSleepDetail.setOnClickListener {
            spendingHistoryViewModel.deleteSingleSpendingHistory(
                spendingHistoryDetailFragmentArgs.userId,
                spendingHistoryDetailFragmentArgs.recordId
            )
            findNavController().navigate(
                SpendDetailFragmentDirections.actionSpendDetailFragmentToSpendingHistoryListFragment(
                    spendingHistoryDetailFragmentArgs.userId
                )
            )
        }
    }

    private fun collectLatestStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                spendingHistoryViewModel.spendingHistoryUiState.collect { state ->
                    if (state.singleSpendHistory != null) {
                        val spendHistory = state.singleSpendHistory
                        spendDetailBinding.dateTxt.text =
                            "Date: ${spendHistory?.date?.getFullDateFromLong() ?: "N/A"}"
                        spendDetailBinding.amountTxt.text =
                            "Amount: ${spendHistory?.amount ?: "N/A"}"
                        spendDetailBinding.descriptionTxt.text =
                            "Category: ${spendHistory?.category ?: "N/A"}"
                    }
                }
            }
        }
    }

}