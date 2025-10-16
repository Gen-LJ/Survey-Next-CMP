package com.lucilab.surveynext.data.repository

import com.lucilab.surveynext.data.model.LoginDataModel
import com.lucilab.surveynext.data.service.datasource.RestClient
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    suspend fun login(email: String, password: String): LoginDataModel
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val service: RestClient
) : AuthRepository {

    override suspend fun login(email: String, password: String): LoginDataModel {
        val response = service.login(email, password)
        if (response.success) {
            return response.data!!
        } else {
            throw Exception(response.message ?: "Login failed")
        }
    }
}