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

    fun getServiceHash(): Int = service.hashCode()

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
        cachedCountries?.let {
            println("Returning Cache Data")
            return it
        }
        val response = service.getRegisterForm()
        if (response.success) {
            println("Returning API data")
            cachedCountries = response.data!!
            return cachedCountries!!
        } else {
            throw Exception(response.message ?: "Retrieve Register Form Failed")
        }
    }
}