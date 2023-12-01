package com.example.firebaseathentication.features.spending_history.presentation.spending_history_listing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebaseathentication.R
import com.example.firebaseathentication.data.local.SpendingHistory
import com.example.firebaseathentication.data.utils.SpendHistoryAdapter
import com.example.firebaseathentication.data.utils.getFullDateFromLong
import com.example.firebaseathentication.databinding.FragmentSpendingHistoryListBinding
import com.example.firebaseathentication.features.spending_history.domain.viewmodel.SpendingHistoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SpendingHistoryListFragment : Fragment() {

    private lateinit var spendingHistoryListBinding: FragmentSpendingHistoryListBinding
    private val spendingHistoryListFragmentArgs: SpendingHistoryListFragmentArgs by navArgs()

    private val spendingHistoryViewModel: SpendingHistoryViewModel by viewModels()

    private lateinit var spendingHistoryAdapter: SpendHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        spendingHistoryListBinding =
            FragmentSpendingHistoryListBinding.inflate(inflater, container, false)
        return spendingHistoryListBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        collectLatestStates()
        spendingHistoryListBinding.addBt.setOnClickListener {
            findNavController().navigate(
                SpendingHistoryListFragmentDirections.actionSpendingHistoryListFragmentToEditSpendingFragment(
                    spendingHistoryListFragmentArgs.userId,
                    null
                )
            )
        }

        spendingHistoryAdapter = SpendHistoryAdapter({ item, view ->

            if (item is SpendingHistory) {
                view.findViewById<TextView>(R.id.history_item_text).text =

                    item.date?.getFullDateFromLong() ?: "N/A"
                view.setOnClickListener {
                    item.id?.let { it1 ->
                        findNavController().navigate(
                            SpendingHistoryListFragmentDirections.actionSpendingHistoryListFragmentToSpendDetailFragment(
                                spendingHistoryListFragmentArgs.userId,
                                it1
                            )
                        )
                    }
                }
            }
        }, R.layout.spending_history_item)
        spendingHistoryListBinding.listingRecycler.layoutManager = LinearLayoutManager(this.context)
        spendingHistoryListBinding.listingRecycler.adapter = spendingHistoryAdapter
        collectLatestStates()
        spendingHistoryViewModel.getAllSpendingHistory(spendingHistoryListFragmentArgs.userId)
    }


    //collect states
    private fun collectLatestStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                spendingHistoryViewModel.spendingHistoryUiState.collect {
                    spendingHistoryListBinding.progressBar.isVisible = it.isLoading == true
                    spendingHistoryListBinding.noDataFoundText.isVisible =
                        it.isLoading != true && it.spendHistoryList?.isEmpty() == true
                    if (!it.spendHistoryList.isNullOrEmpty())
                        spendingHistoryAdapter.setData(it.spendHistoryList)
                }
            }
        }
    }

}