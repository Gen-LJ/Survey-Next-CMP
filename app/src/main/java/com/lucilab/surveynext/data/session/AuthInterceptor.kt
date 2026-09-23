package com.lucilab.surveynext.data.session

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Attaches the bearer token, and ends the session when the backend rejects it.
 * Only authenticated requests clear the session: a 401 from /auth/login just
 * means wrong credentials.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionManager.token
        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        val response = chain.proceed(request)
        if (response.code == 401 && token != null) {
            sessionManager.clear()
        }
        return response
    }
}
