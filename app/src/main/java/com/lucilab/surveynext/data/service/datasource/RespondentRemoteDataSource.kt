package com.lucilab.surveynext.data.service.datasource

import com.lucilab.surveynext.data.model.AnswerDetailsModel
import com.lucilab.surveynext.data.model.AnswerModel
import com.lucilab.surveynext.data.model.PageModel
import com.lucilab.surveynext.data.model.RespondentHomeModel
import com.lucilab.surveynext.data.model.StatusResponseModel
import com.lucilab.surveynext.data.model.SurveyModel
import com.lucilab.surveynext.data.service.requestModel.SubmitAnswerRequestModel
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

private interface RespondentApi {
    @GET("respondent/home")
    suspend fun home(): StatusResponseModel<RespondentHomeModel>

    @GET("respondent/survey/list")
    suspend fun availableSurveys(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
    ): StatusResponseModel<PageModel<SurveyModel>>

    @GET("respondent/survey/{id}")
    suspend fun surveyDetail(@Path("id") id: Int): StatusResponseModel<SurveyModel>

    @POST("respondent/survey/{id}/answer")
    suspend fun submitAnswer(
        @Path("id") id: Int,
        @Body request: SubmitAnswerRequestModel,
    ): StatusResponseModel<AnswerModel>

    @GET("respondent/saved")
    suspend fun savedSurveys(): StatusResponseModel<List<SurveyModel>>

    @GET("respondent/saved-ids")
    suspend fun savedSurveyIds(): StatusResponseModel<List<Int>>

    @POST("respondent/saved/{id}")
    suspend fun saveSurvey(@Path("id") id: Int): StatusResponseModel<Any>

    @DELETE("respondent/saved/{id}")
    suspend fun removeSavedSurvey(@Path("id") id: Int): StatusResponseModel<Any>

    @GET("respondent/completed")
    suspend fun completed(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
    ): StatusResponseModel<PageModel<SurveyModel>>

    @GET("respondent/completed/{id}")
    suspend fun answerDetails(@Path("id") id: Int): StatusResponseModel<AnswerDetailsModel>
}

@Singleton
class RespondentRemoteDataSource @Inject constructor(
    retrofit: Retrofit
) : BaseRemoteDataSource() {

    private val api = retrofit.create(RespondentApi::class.java)

    suspend fun home() = safeApiCall { api.home() }

    /** Published, unfilled surveys in the respondent's own country and region. */
    suspend fun availableSurveys(page: Int, limit: Int) = safeApiCall { api.availableSurveys(page, limit) }

    /** Fails with 409 when the survey is closed or already answered. */
    suspend fun surveyDetail(id: Int) = safeApiCall { api.surveyDetail(id) }

    suspend fun submitAnswer(id: Int, request: SubmitAnswerRequestModel) =
        safeApiCall { api.submitAnswer(id, request) }

    suspend fun savedSurveys() = safeApiCall { api.savedSurveys() }

    suspend fun savedSurveyIds() = safeApiCall { api.savedSurveyIds() }

    /** At most five surveys can be saved; the sixth fails with 409. */
    suspend fun saveSurvey(id: Int) = safeApiCall { api.saveSurvey(id) }

    suspend fun removeSavedSurvey(id: Int) = safeApiCall { api.removeSavedSurvey(id) }

    suspend fun completed(page: Int, limit: Int) = safeApiCall { api.completed(page, limit) }

    suspend fun answerDetails(id: Int) = safeApiCall { api.answerDetails(id) }
}
