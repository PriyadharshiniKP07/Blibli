package com.example.bliblisearch.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.bliblisearch.R
import com.example.bliblisearch.databinding.FragmentLoginBinding
import com.example.bliblisearch.view.SharedPreferenceManager
import com.example.bliblisearch.viewModel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()

        val username = binding.etemail.text.toString().trim()
        binding.passwordlayout.visibility =
            if (SharedPreferenceManager.isUserExists(requireContext(), username)) View.VISIBLE else View.GONE
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(viewLifecycleOwner) { success ->
            if (success) {

                val username = binding.etemail.text.toString().trim()
                SharedPreferenceManager.setLoggedInUser(requireContext(), username)

                Toast.makeText(requireContext(), "Login successful 🎉", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), CartActivity::class.java))
                activity?.finish()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        binding.passwordlayout.visibility = View.GONE

        binding.etemail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val user = s.toString().trim()
                binding.passwordlayout.visibility =
                    if (SharedPreferenceManager.isUserExists(requireContext(), user)) View.VISIBLE else View.GONE
            }
        })

        binding.btnlogin.setOnClickListener {
            val username = binding.etemail.text.toString().trim()
            val password = binding.etpassword.text.toString().trim()

            if (username.isEmpty()) {
                binding.emaillayout.error = "Required"
                return@setOnClickListener
            }

            if (binding.passwordlayout.isVisible && password.isEmpty()) {
                binding.passwordlayout.error = "Required"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(username).matches() &&
                !Patterns.PHONE.matcher(username).matches()
            ) {
                binding.emaillayout.error = "Invalid email/phone"
                return@setOnClickListener
            }

            viewModel.login(requireContext(), username, password)
        }

        binding.register.setOnClickListener {
            val prefill = binding.etemail.text.toString().trim()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegisterFragment().apply {
                    arguments = Bundle().apply { putString("prefill_value", prefill) }
                })
                .addToBackStack(null)
                .commit()
        }
    }
}
