package com.lucilab.surveynext.data.repository

import android.util.Log
import com.lucilab.surveynext.data.model.CountryModel
import com.lucilab.surveynext.data.model.LoginDataModel
import com.lucilab.surveynext.data.model.UserModel
import com.lucilab.surveynext.data.model.requireData
import com.lucilab.surveynext.data.service.datasource.RestClient
import com.lucilab.surveynext.data.session.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    /** Signs in and persists the session. */
    suspend fun login(email: String, password: String): LoginDataModel

    suspend fun register(
        name: String, email: String, password: String, role: String, countryId: UInt, regionId: UInt
    ): UserModel

    suspend fun getRegisterForm(): List<CountryModel>

    /** Re-reads the signed-in user, keeping points in the session current. */
    suspend fun refreshUser(): UserModel

    /** "Region, Country" for the ids, or null when the lookup fails. */
    suspend fun locationName(countryId: Int, regionId: Int): String?

    fun logout()
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val service: RestClient,
    private val sessionManager: SessionManager,
) : AuthRepository {

    //In-memory cache
    private var cachedCountries: List<CountryModel>? = null

    override suspend fun login(email: String, password: String): LoginDataModel {
        val data = service.login(email, password).requireData("Login failed")
        sessionManager.save(data.token, data.user)
        return data
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        countryId: UInt,
        regionId: UInt
    ): UserModel {
        return service.register(name, email, password, role, countryId, regionId)
            .requireData("Register Failed")
    }

    override suspend fun getRegisterForm(): List<CountryModel> {
        cachedCountries?.let {
            Log.d("Cache", "Returning Cache Data")
            return it
        }
        val countries = service.getRegisterForm().requireData("Retrieve Register Form Failed")
        Log.d("Cache", "Saving in memory cache from API data")
        cachedCountries = countries
        return countries
    }

    override suspend fun refreshUser(): UserModel {
        val user = service.me().requireData("Couldn't load your account")
        sessionManager.updateUser(user)
        return user
    }

    override suspend fun locationName(countryId: Int, regionId: Int): String? = runCatching {
        val country = getRegisterForm().firstOrNull { it.id.toInt() == countryId } ?: return null
        val region = country.regions.orEmpty().firstOrNull { it.id.toInt() == regionId }
        listOfNotNull(region?.name, country.name).joinToString(", ")
    }.getOrNull()

    override fun logout() {
        sessionManager.clear()
    }
}
