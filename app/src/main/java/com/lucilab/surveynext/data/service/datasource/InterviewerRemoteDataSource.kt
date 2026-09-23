package com.lucilab.surveynext.data.service.datasource

import com.lucilab.surveynext.data.model.AnalyticsModel
import com.lucilab.surveynext.data.model.CreateSurveyFormModel
import com.lucilab.surveynext.data.model.InterviewerHomeModel
import com.lucilab.surveynext.data.model.PageModel
import com.lucilab.surveynext.data.model.QuestionModel
import com.lucilab.surveynext.data.model.StatusResponseModel
import com.lucilab.surveynext.data.model.SurveyModel
import com.lucilab.surveynext.data.service.requestModel.AddQuestionRequestModel
import com.lucilab.surveynext.data.service.requestModel.CreateSurveyRequestModel
import com.lucilab.surveynext.data.service.requestModel.EditQuestionRequestModel
import com.lucilab.surveynext.data.service.requestModel.EditSurveyInfoRequestModel
import com.lucilab.surveynext.data.service.requestModel.PauseSurveyRequestModel
import com.lucilab.surveynext.data.service.requestModel.PublishSurveyRequestModel
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

private interface InterviewerApi {
    @GET("interviewer/home")
    suspend fun home(): StatusResponseModel<InterviewerHomeModel>

    @GET("interviewer/completed")
    suspend fun completed(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
    ): StatusResponseModel<PageModel<SurveyModel>>

    @GET("interviewer/survey/form")
    suspend fun createSurveyForm(): StatusResponseModel<CreateSurveyFormModel>

    @POST("interviewer/survey/create")
    suspend fun createSurvey(@Body request: CreateSurveyRequestModel): StatusResponseModel<SurveyModel>

    @GET("interviewer/survey/list")
    suspend fun listSurveys(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("state") state: String?,
    ): StatusResponseModel<PageModel<SurveyModel>>

    @GET("interviewer/survey/{id}")
    suspend fun getSurvey(@Path("id") id: Int): StatusResponseModel<SurveyModel>

    @PUT("interviewer/survey/{id}")
    suspend fun editSurveyInfo(
        @Path("id") id: Int,
        @Body request: EditSurveyInfoRequestModel,
    ): StatusResponseModel<Any>

    @DELETE("interviewer/survey/{id}")
    suspend fun deleteSurvey(@Path("id") id: Int): StatusResponseModel<Any>

    @POST("interviewer/survey/{id}/publish")
    suspend fun publishSurvey(
        @Path("id") id: Int,
        @Body request: PublishSurveyRequestModel,
    ): StatusResponseModel<Any>

    @POST("interviewer/survey/{id}/pause")
    suspend fun pauseSurvey(
        @Path("id") id: Int,
        @Body request: PauseSurveyRequestModel,
    ): StatusResponseModel<Any>

    @GET("interviewer/survey/{id}/analytics")
    suspend fun analytics(@Path("id") id: Int): StatusResponseModel<AnalyticsModel>

    @POST("interviewer/survey/{id}/questions")
    suspend fun addQuestion(
        @Path("id") surveyId: Int,
        @Body request: AddQuestionRequestModel,
    ): StatusResponseModel<QuestionModel>

    @PUT("interviewer/survey/{id}/questions/{question_id}")
    suspend fun editQuestion(
        @Path("id") surveyId: Int,
        @Path("question_id") questionId: Int,
        @Body request: EditQuestionRequestModel,
    ): StatusResponseModel<Any>

    @DELETE("interviewer/survey/{id}/questions/{question_id}")
    suspend fun deleteQuestion(
        @Path("id") surveyId: Int,
        @Path("question_id") questionId: Int,
    ): StatusResponseModel<Any>
}

@Singleton
class InterviewerRemoteDataSource @Inject constructor(
    retrofit: Retrofit
) : BaseRemoteDataSource() {

    private val api = retrofit.create(InterviewerApi::class.java)

    suspend fun home() = safeApiCall { api.home() }

    suspend fun completed(page: Int, limit: Int) = safeApiCall { api.completed(page, limit) }

    suspend fun createSurveyForm() = safeApiCall { api.createSurveyForm() }

    suspend fun createSurvey(request: CreateSurveyRequestModel) = safeApiCall { api.createSurvey(request) }

    /**
     * @param state a survey state, or null for every state
     */
    suspend fun listSurveys(page: Int, limit: Int, state: String?) =
        safeApiCall { api.listSurveys(page, limit, state) }

    suspend fun getSurvey(id: Int) = safeApiCall { api.getSurvey(id) }

    suspend fun editSurveyInfo(id: Int, request: EditSurveyInfoRequestModel) =
        safeApiCall { api.editSurveyInfo(id, request) }

    suspend fun deleteSurvey(id: Int) = safeApiCall { api.deleteSurvey(id) }

    suspend fun publishSurvey(id: Int, request: PublishSurveyRequestModel) =
        safeApiCall { api.publishSurvey(id, request) }

    suspend fun pauseSurvey(id: Int, paused: Boolean) =
        safeApiCall { api.pauseSurvey(id, PauseSurveyRequestModel(paused)) }

    suspend fun analytics(id: Int) = safeApiCall { api.analytics(id) }

    suspend fun addQuestion(surveyId: Int, request: AddQuestionRequestModel) =
        safeApiCall { api.addQuestion(surveyId, request) }

    suspend fun editQuestion(surveyId: Int, questionId: Int, request: EditQuestionRequestModel) =
        safeApiCall { api.editQuestion(surveyId, questionId, request) }

    suspend fun deleteQuestion(surveyId: Int, questionId: Int) =
        safeApiCall { api.deleteQuestion(surveyId, questionId) }
}
