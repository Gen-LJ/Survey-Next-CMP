package com.lucilab.surveynext.data.service.datasource

import com.google.gson.JsonParser
import com.lucilab.surveynext.data.model.StatusResponseModel
import retrofit2.HttpException

abstract class BaseRemoteDataSource {
    protected suspend fun <T> safeApiCall(apiCall: suspend () -> StatusResponseModel<T>): StatusResponseModel<T> {
        return try {
            apiCall()
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val message = try {
                JsonParser.parseString(errorBody)
                    .asJsonObject["message"]?.asString ?: "Unknown error"
            } catch (_: Exception) {
                "Unknown error"
            }
            StatusResponseModel(success = false, message = message, data = null)
        } catch (e: Exception) {
            StatusResponseModel(success = false, message = e.message ?: "Network error", data = null)
        }
    }
}
