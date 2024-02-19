package com.example.firebaseathentication.features.auth.presentation.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.firebaseathentication.databinding.FragmentRegistrationBinding
import com.example.firebaseathentication.features.auth.domain.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegistrationFragment : Fragment() {
    private lateinit  var _binding: FragmentRegistrationBinding
    private val binding get() = _binding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.regBt.setOnClickListener {
            authViewModel.register(
                binding.emailReg.editText?.text.toString(),
                binding.passwordReg.editText?.text.toString(),
                binding.confirmPasswordReg.editText?.text.toString(),
                binding.nameReg.editText?.text.toString()
            )
        }
        collectLatestStates()
    }

    private fun collectLatestStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                authViewModel.authUiState.collectLatest { state ->
                    binding.regBt.isEnabled = !state.isLoading
                    if (state.isSuccessful) {
                        findNavController().navigate(
                            RegistrationFragmentDirections.actionRegistrationFragmentToSpendingHistoryListFragment(
                                state.userId!!
                            )
                        )
                    }
                    state.error?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    }
                    authViewModel.resetState()
                }
            }
        }
    }

}