package com.example.firebaseathentication.features.auth.presentation.login

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
import com.example.firebaseathentication.R
import com.example.firebaseathentication.databinding.FragmentLoginBinding
import com.example.firebaseathentication.features.auth.domain.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private lateinit var _binding: FragmentLoginBinding
    private val binding get() = _binding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding.root
    }

    //call login method from viewmodel to login user, and navigate to home fragment if successful
    // if user doesn't have an account and wants to register, navigate to registration fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectLatestStates()
        binding.loginBt.setOnClickListener {
            authViewModel.login(
                binding.emailLogin.editText?.text.toString(),
                binding.passwordLogin.editText?.text.toString()
            )
        }
        binding.textView3.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
        }
    }

    //collect latest states, and navigate to home fragment if successful
    private fun collectLatestStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                authViewModel.authUiState.collectLatest { state ->
                    binding.loginBt.isEnabled = !state.isLoading
                    if (state.isSuccessful) {
                        findNavController().navigate(
                            LoginFragmentDirections.actionLoginFragmentToSpendingHistoryListFragment(
                                state.userId!!
                            )
                        )
                    }
                    if (state.error != null) {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }
                    authViewModel.resetState()
                }
            }
        }
    }

}