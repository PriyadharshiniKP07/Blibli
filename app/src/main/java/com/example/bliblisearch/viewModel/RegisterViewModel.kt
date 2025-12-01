package com.example.bliblisearch.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bliblisearch.view.SharedPreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel() {

    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> get() = _registerSuccess

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun registerUser(context: Context, name: String, phone: String, email: String, password: String) {

        if (SharedPreferenceManager.isUserExists(context, email)) {
            _errorMessage.value = "User already exists"
            _registerSuccess.value = false
            return
        }

        val saved = SharedPreferenceManager.saveUser(context, email, password)

        if (saved) {
            SharedPreferenceManager.setLoggedInUser(context, email)
            _registerSuccess.value = true
        } else {
            _errorMessage.value = "Failed to register user"
            _registerSuccess.value = false
        }
    }
}
