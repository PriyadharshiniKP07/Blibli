package com.example.bliblisearch.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bliblisearch.view.SharedPreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun login(context: Context, username: String, password: String) {
        if (!SharedPreferenceManager.isUserExists(context, username)) {
            _errorMessage.value = "User not found"
            _loginResult.value = false
            return
        }

        if (SharedPreferenceManager.validateUser(context, username, password)) {
            SharedPreferenceManager.setLoggedInUser(context, username)
            _loginResult.value = true
        } else {
            _errorMessage.value = "Incorrect password"
            _loginResult.value = false
        }
    }
}
