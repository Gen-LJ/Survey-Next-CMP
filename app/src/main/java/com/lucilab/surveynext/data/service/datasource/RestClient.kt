package com.lucilab.surveynext.data.service.datasource

import com.lucilab.surveynext.data.model.CountryModel
import com.lucilab.surveynext.data.model.LoginDataModel
import com.lucilab.surveynext.data.model.RegionModel
import com.lucilab.surveynext.data.model.StatusResponseModel
import com.lucilab.surveynext.data.model.UserModel
import com.lucilab.surveynext.data.service.requestModel.LoginRequestModel
import com.lucilab.surveynext.data.service.requestModel.RegisterRequestModel
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import javax.inject.Inject
import javax.inject.Singleton

private interface RestApi {
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestModel
    ): StatusResponseModel<LoginDataModel>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequestModel
    ): StatusResponseModel<UserModel>

    @GET("auth/register-form")
    suspend fun getRegisterForm(): StatusResponseModel<List<CountryModel>>

    @GET("me")
    suspend fun me(): StatusResponseModel<UserModel>

    @GET("regions/{country_id}")
    suspend fun getRegions(
        @Path("country_id") countryId: Int
    ): StatusResponseModel<List<RegionModel>>
}

@Singleton
class RestClient @Inject constructor(
    retrofit: Retrofit
) : BaseRemoteDataSource() {

    private val api = retrofit.create(RestApi::class.java)

    /**
     * Performs login API call
     *
     * @return [StatusResponseModel] containing [LoginDataModel] on success data field
     */
    suspend fun login(
        email: String, password: String
    ): StatusResponseModel<LoginDataModel> = safeApiCall {
        api.login(LoginRequestModel(email, password))
    }

    /**
     * Performs registration API call
     *
     * @return [StatusResponseModel] containing [UserModel] on success data field
     */
    suspend fun register(
        name: String, email: String, password: String, role: String, countryId: UInt, regionId: UInt
    ): StatusResponseModel<UserModel> = safeApiCall {
        api.register(
            RegisterRequestModel(
                name = name,
                email = email,
                password = password,
                role = role,
                countryId = countryId,
                regionId = regionId
            )
        )
    }

    /**
     * Performs retrieve necessary data for register form API call
     *
     * @return [StatusResponseModel] containing List of [CountryModel] on success data field
     */
    suspend fun getRegisterForm(
    ): StatusResponseModel<List<CountryModel>> = safeApiCall {
        api.getRegisterForm()
    }

    /**
     * Fetches the signed-in user
     *
     * @return [StatusResponseModel] containing [UserModel] on success data field
     */
    suspend fun me(): StatusResponseModel<UserModel> = safeApiCall {
        api.me()
    }

    /**
     * Fetches the active regions of a country
     *
     * @return [StatusResponseModel] containing List of [RegionModel] on success data field
     */
    suspend fun getRegions(countryId: Int): StatusResponseModel<List<RegionModel>> = safeApiCall {
        api.getRegions(countryId)
    }
}
