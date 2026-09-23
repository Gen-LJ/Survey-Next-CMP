package com.lucilab.surveynext.data.service.datasource

import com.google.gson.JsonParser
import com.lucilab.surveynext.data.model.StatusResponseModel
import retrofit2.HttpException
import java.io.IOException

abstract class BaseRemoteDataSource {
    protected suspend fun <T> safeApiCall(apiCall: suspend () -> StatusResponseModel<T>): StatusResponseModel<T> {
        return try {
            apiCall()
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val message = try {
                // Handlers answer with "message"; the role middleware with "error".
                val json = JsonParser.parseString(errorBody).asJsonObject
                (json["message"] ?: json["error"])?.asString
            } catch (_: Exception) {
                null
            } ?: if (e.code() == 401) "Your session has expired" else "Unknown error"
            StatusResponseModel(success = false, message = message.replaceFirstChar { it.uppercase() }, data = null)
        } catch (_: IOException) {
            StatusResponseModel(success = false, message = "Can't reach the server. Check your connection.", data = null)
        } catch (e: Exception) {
            StatusResponseModel(success = false, message = e.message ?: "Network error", data = null)
        }
    }
}
