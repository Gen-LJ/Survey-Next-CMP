package com.lucilab.surveynext.data.session

import android.content.Context
import com.google.gson.Gson
import com.lucilab.surveynext.data.model.UserModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the signed-in user and their token. Backed by SharedPreferences so the
 * session survives restarts, and read synchronously by the auth interceptor.
 */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context,
    private val gson: Gson,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _user = MutableStateFlow(readUser())
    val user: StateFlow<UserModel?> = _user.asStateFlow()

    val token: String? get() = prefs.getString(KEY_TOKEN, null)

    fun save(token: String, user: UserModel) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER, gson.toJson(user))
            .apply()
        _user.value = user
    }

    fun updateUser(user: UserModel) {
        if (token == null) return
        prefs.edit().putString(KEY_USER, gson.toJson(user)).apply()
        _user.value = user
    }

    fun clear() {
        prefs.edit().clear().apply()
        _user.value = null
    }

    private fun readUser(): UserModel? {
        if (prefs.getString(KEY_TOKEN, null) == null) return null
        return runCatching {
            gson.fromJson(prefs.getString(KEY_USER, null), UserModel::class.java)
        }.getOrNull()
    }

    private companion object {
        const val PREFS_NAME = "session"
        const val KEY_TOKEN = "token"
        const val KEY_USER = "user"
    }
}
