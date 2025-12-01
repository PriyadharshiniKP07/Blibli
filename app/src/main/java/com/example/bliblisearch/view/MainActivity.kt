package com.example.bliblisearch.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bliblisearch.view.SharedPreferenceManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loggedUser = SharedPreferenceManager.getLoggedInUser(this)

        if (loggedUser.isNullOrEmpty()) {
            // User is NOT logged in -> Open Login Activity
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            // User already logged in -> Take to Cart directly
            startActivity(Intent(this, CartActivity::class.java))
        }

        finish() // so user can't return to MainActivity
    }

    override fun onStart() {
        super.onStart()

        val user = SharedPreferenceManager.getLoggedInUser(this)
        if (user != null) {
            startActivity(Intent(this, CartActivity::class.java))
            finish()
        }
    }

}
