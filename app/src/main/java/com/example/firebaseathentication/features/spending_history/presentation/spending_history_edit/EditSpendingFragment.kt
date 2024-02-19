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
import com.example.firebaseathentication.R
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

    private lateinit var result: ActivityResultLauncher<String>
    var currentPhotoPath: String? = null
    var isTakingPicture = false


    override fun onAttach(context: Context) {
        super.onAttach(context)
        result = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted == true && isTakingPicture) {
                isTakingPicture = false
                dispatchTakePictureIntent()
            }
        }
    }

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

        val cameraPermissionGranted = checkCameraPermission()
        if (!cameraPermissionGranted) {
            requestCameraPermission()
        }
        editSpendingBinding.takePhotoTextView.setOnClickListener {
            val newCameraPermissionGranted = checkCameraPermission()
            Log.d("Tag", "The camera permission status: $newCameraPermissionGranted")
            if (!newCameraPermissionGranted) {
                isTakingPicture = true
                requestCameraPermission()
            } else {
                dispatchTakePictureIntent()
                Toast.makeText(
                    requireActivity(),
                    "The camera permission has not been granted",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_DENIED
    }

    private fun requestCameraPermission(onGranted: ((Boolean) -> Unit)? = null) {
        Log.d("Tag", "Launch request Permission")
        result.launch(Manifest.permission.CAMERA)

    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        Log.d("TAG", "dispatchTakePictureIntent: ")
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent

            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                // Create the File where the photo should go
                Log.d("TAG", "dispatchTakePictureIntent: dispatchTakePictureIntent")
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Log.e("TAG", "dispatchTakePictureIntent: ${ex.message}")
                    null
                }
                Log.e("TAG", "dispatchTakePictureIntent: ${photoFile?.absolutePath}")
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireActivity(),
                        "com.example.firebaseathentication.fileprovider",
                        it
                    )
                    //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, 1234)
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1234 && resultCode == RESULT_OK && data != null) {
            val imageBitmap = data.extras?.get("data") as Bitmap
            editSpendingBinding.spendingImageView.setImageBitmap(imageBitmap)
        }
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

                if (spendAmount != null && selectedDate != null) {

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
            editSpendingBinding.textView.text = getString(R.string.edit_spend_record)
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
                    if (it.updatedSpendHistory != null && spendingHistoryEditFragmentArgs.recordId != null) {
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