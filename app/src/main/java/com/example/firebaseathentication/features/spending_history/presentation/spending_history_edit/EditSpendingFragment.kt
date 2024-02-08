package com.example.firebaseathentication.features.spending_history.presentation.spending_history_edit

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.firebaseathentication.data.utils.getFullDateFromLong
import com.example.firebaseathentication.databinding.FragmentEditSpendingBinding
import com.example.firebaseathentication.features.spending_history.domain.viewmodel.SpendingHistoryViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

@AndroidEntryPoint
class EditSpendingFragment : Fragment() {

    private lateinit var editSpendingBinding: FragmentEditSpendingBinding
    private val spendingHistoryEditFragmentArgs: EditSpendingFragmentArgs by navArgs()
    private val spendingHistoryViewModel: SpendingHistoryViewModel by viewModels()

    private var selectedDate: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        editSpendingBinding = FragmentEditSpendingBinding.inflate(inflater, container, false)

        return editSpendingBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectStates()
        setViews()
    }


    private fun setViews() {
        editSpendingBinding.spendDate.editText?.isFocusable = false
        editSpendingBinding.spendDate.editText?.setOnClickListener {
            val dateValidator: CalendarConstraints.DateValidator =
                DateValidatorPointBackward.before(MaterialDatePicker.todayInUtcMilliseconds())

            val constraintsBuilder =
                CalendarConstraints.Builder()
                    .setStart(MaterialDatePicker.todayInUtcMilliseconds())
                    .setValidator(dateValidator)

            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Date")
                    .setCalendarConstraints(constraintsBuilder.build())
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()


            datePicker.show(parentFragmentManager, "Tag")
            datePicker.addOnPositiveButtonClickListener { date ->
                selectedDate = date
                editSpendingBinding.spendDate.editText?.setText(date.getFullDateFromLong())

            }
        }

        if (spendingHistoryEditFragmentArgs.recordId == null) {
            //add new spend record

            editSpendingBinding.saveSpendingRecord.setOnClickListener {
                var spendAmount =
                    editSpendingBinding.spendAmount.editText?.text.toString().toFloatOrNull()
                var spendDescription =
                    editSpendingBinding.spendDescription.editText?.text.toString()

                if (spendAmount != null && spendAmount != null && selectedDate != null) {

                    spendingHistoryViewModel.addSpendingHistory(
                        spendingHistoryEditFragmentArgs.userId,
                        selectedDate!!,
                        spendAmount,
                        spendDescription
                    )
                }
            }
        } else {
            //update existing record
            editSpendingBinding.textView.setText("Edit Spend Record")
            spendingHistoryViewModel.getSingleSpendingHistory(
                spendingHistoryEditFragmentArgs.userId,
                spendingHistoryEditFragmentArgs.recordId!!
            )

            editSpendingBinding.saveSpendingRecord.setOnClickListener {
                var spendAmount =
                    editSpendingBinding.spendAmount.editText?.text.toString().toFloatOrNull()
                var spendDescription =
                    editSpendingBinding.spendDescription.editText?.text.toString()

                spendingHistoryViewModel.updateSpendingHistory(
                    spendingHistoryEditFragmentArgs.userId,
                    spendingHistoryEditFragmentArgs.recordId!!,
                    selectedDate,
                    spendAmount,
                    spendDescription
                )
            }
        }
    }

    private fun collectStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                spendingHistoryViewModel.spendingHistoryUiState.collect() {

                    if (it.singleSpendHistory != null) {
                        editSpendingBinding.spendDate.editText?.setText(it.singleSpendHistory.date?.getFullDateFromLong())
                        editSpendingBinding.spendAmount.editText?.setText(it.singleSpendHistory.amount.toString())
                        editSpendingBinding.spendDescription.editText?.setText(it.singleSpendHistory.description.toString())
                    }

                    if (it.createdSpendHistory != null && spendingHistoryEditFragmentArgs.recordId == null) {
                        spendingHistoryViewModel.resetState()
                        findNavController().navigate(
                            EditSpendingFragmentDirections.actionEditSpendingFragmentToSpendDetailFragment(
                                spendingHistoryEditFragmentArgs.userId,
                                it.createdSpendHistory.id!!
                            )

                        )

                    }
                    if (it.updatedSpendHistory != null && spendingHistoryEditFragmentArgs.recordId != null){
                        spendingHistoryViewModel.resetState()
                        findNavController().navigate(
                            EditSpendingFragmentDirections.actionEditSpendingFragmentToSpendDetailFragment(
                                spendingHistoryEditFragmentArgs.userId,
                                it.updatedSpendHistory.id!!
                            )

                        )
                    }
                }
            }
        }
    }
}