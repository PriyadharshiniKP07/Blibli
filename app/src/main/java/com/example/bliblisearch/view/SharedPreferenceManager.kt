package com.example.bliblisearch.view

import android.content.Context
import org.json.JSONException
import org.json.JSONObject
import kotlin.text.isNullOrEmpty

object SharedPreferenceManager {
    private val PREFS_NAME = "app_prefs"
    private  val KEY_USERS = "users"            // JSON map: { "user1":"pass1", ... }
    private  val KEY_CURRENT_USER = "current_user"
    private const val CART_PREFIX = "CART_USER_"


    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveUser(context: Context, username: String, password: String): Boolean {
        val sp = prefs(context)
        val usersJson = sp.getString(KEY_USERS, null)
        val users = if (!usersJson.isNullOrEmpty()) JSONObject(usersJson) else JSONObject()

        if (users.has(username)) {
            // user exists
            return false
        }
        users.put(username, password)
        sp.edit().putString(KEY_USERS, users.toString()).apply()
        return true
    }

    fun validateUser(context: Context, username: String, password: String): Boolean {
        val sp = prefs(context)
        val usersJson = sp.getString(KEY_USERS, null) ?: return false
        return try {
            val users = JSONObject(usersJson)
            if (!users.has(username)) return false
            users.getString(username) == password
        } catch (e: JSONException) {
            false
        }
    }

    fun isUserExists(context: Context, username: String): Boolean {
        val sp = prefs(context)
        val usersJson = sp.getString(KEY_USERS, null) ?: return false
        return try {
            val users = JSONObject(usersJson)
            users.has(username)
        } catch (e: JSONException) {
            false
        }
    }

    fun setLoggedInUser(context: Context, username: String) {
        prefs(context).edit().putString(KEY_CURRENT_USER, username).apply()
    }


    fun getLoggedInUser(context: Context): String? =
        prefs(context).getString(KEY_CURRENT_USER, null)


    fun clearLoggedInUser(context: Context) {
        prefs(context).edit().remove(KEY_CURRENT_USER).apply()
    }


    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }

    fun saveCartForUser(context: Context, username: String, cartJson: String) {
        prefs(context).edit().putString("$CART_PREFIX$username", cartJson).apply()
    }

    fun getCartForUser(context: Context, username: String): String? {
        return prefs(context).getString("$CART_PREFIX$username", null)
    }


}

