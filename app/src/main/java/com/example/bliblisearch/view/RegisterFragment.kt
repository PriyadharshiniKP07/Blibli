package com.example.bliblisearch.view

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.bliblisearch.databinding.FragmentRegisterBinding
import com.example.bliblisearch.view.SharedPreferenceManager
import com.example.bliblisearch.viewModel.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        arguments?.getString("prefill_value")?.let {
            binding.etemail.setText(it)
        }

        setupObservers()

        binding.btnregister.setOnClickListener { validateAndRegister() }
    }

    private fun setupObservers() {
        viewModel.registerSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {

                val email = binding.etemail.text.toString().trim()
                SharedPreferenceManager.setLoggedInUser(requireContext(), email)

                Toast.makeText(requireContext(), "Registered Successfully 🎉", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), CartActivity::class.java))
                requireActivity().finish()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateAndRegister() {
        val name = binding.etname.text.toString().trim()
        val phone = binding.etph.text.toString().trim()
        val email = binding.etemail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        when {
            name.isEmpty() -> showError("Name is required")
            phone.isEmpty() -> showError("Phone is required")
            email.isEmpty() -> showError("Email is required")
            password.isEmpty() -> showError("Password is required")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> showError("Invalid email format")
            else -> viewModel.registerUser(requireContext(), name, phone, email, password)
        }
    }

    private fun showError(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
