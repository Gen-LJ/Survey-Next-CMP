package com.lucilab.surveynext.data.service.datasource

import com.lucilab.surveynext.data.model.LoginDataModel
import com.lucilab.surveynext.data.model.StatusResponseModel
import com.lucilab.surveynext.data.service.requestModel.LoginRequestModel
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton

private interface RestApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestModel): StatusResponseModel<LoginDataModel>
}

@Singleton
class RestClient @Inject constructor(
    retrofit: Retrofit
) {
    private val api = retrofit.create(RestApi::class.java)

    suspend fun login(email: String, password: String): StatusResponseModel<LoginDataModel> {
        return try {
            val response = api.login(LoginRequestModel(email, password))
            // API returned 2xx response, return as is
            response
        } catch (e: retrofit2.HttpException) {
            // API returned non-2xx response
            val errorBody = e.response()?.errorBody()?.string()
            val message = try {
                // Parse JSON error message
                val json = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                json.get("message")?.asString ?: "Unknown error"
            } catch (_: Exception) {
                "Unknown error"
            }
            StatusResponseModel(
                success = false,
                message = message,
                data = null
            )
        } catch (e: Exception) {
            // Network or unexpected errors
            StatusResponseModel(
                success = false,
                message = e.message ?: "Network error",
                data = null
            )
        }
    }
}

