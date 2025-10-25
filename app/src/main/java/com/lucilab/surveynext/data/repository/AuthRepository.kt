package com.lucilab.surveynext.data.repository

import android.util.Log
import com.lucilab.surveynext.data.model.CountryModel
import com.lucilab.surveynext.data.model.LoginDataModel
import com.lucilab.surveynext.data.model.UserModel
import com.lucilab.surveynext.data.service.datasource.RestClient
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    suspend fun login(email: String, password: String): LoginDataModel

    suspend fun register(
        name: String, email: String, password: String, role: String, countryId: UInt, regionId: UInt
    ): UserModel

    suspend fun getRegisterForm(): List<CountryModel>
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val service: RestClient
) : AuthRepository {

    //In-memory cache
    private var cachedCountries: List<CountryModel>? = null

    override suspend fun login(email: String, password: String): LoginDataModel {
        val response = service.login(email, password)
        if (response.success) {
            return response.data!!
        } else {
            throw Exception(response.message ?: "Login failed")
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        countryId: UInt,
        regionId: UInt
    ): UserModel {
        val response = service.register(name, email, password, role, countryId, regionId)
        if (response.success) {
            return response.data!!
        } else {
            throw Exception(response.message ?: "Register Failed")
        }
    }

    override suspend fun getRegisterForm(): List<CountryModel> {
        try {
            cachedCountries?.let {
                Log.d("Cache", "Returning Cache Data")
                return it
            }
            val response = service.getRegisterForm()
            if (response.success) {
                Log.d("Cache", "Saving in memory cache from API data")
                cachedCountries = response.data!!
                return cachedCountries!!
            } else {
                throw Exception(response.message ?: "Retrieve Register Form Failed")
            }
        } catch (e: Exception) {
            throw Exception(e.message ?: "Retrieve Register Form Failed")
        }
    }
}