package com.lucilab.surveynext.data.repository

import com.lucilab.surveynext.data.model.AnalyticsModel
import com.lucilab.surveynext.data.model.CreateSurveyFormModel
import com.lucilab.surveynext.data.model.InterviewerHomeModel
import com.lucilab.surveynext.data.model.PageModel
import com.lucilab.surveynext.data.model.QuestionModel
import com.lucilab.surveynext.data.model.QuestionType
import com.lucilab.surveynext.data.model.RegionModel
import com.lucilab.surveynext.data.model.SurveyModel
import com.lucilab.surveynext.data.model.SurveyState
import com.lucilab.surveynext.data.model.requireData
import com.lucilab.surveynext.data.model.requireSuccess
import com.lucilab.surveynext.data.service.datasource.InterviewerRemoteDataSource
import com.lucilab.surveynext.data.service.datasource.RestClient
import com.lucilab.surveynext.data.service.requestModel.AddQuestionRequestModel
import com.lucilab.surveynext.data.service.requestModel.CreateSurveyRequestModel
import com.lucilab.surveynext.data.service.requestModel.EditQuestionRequestModel
import com.lucilab.surveynext.data.service.requestModel.EditSurveyInfoRequestModel
import com.lucilab.surveynext.data.service.requestModel.PublishSurveyRequestModel
import com.lucilab.surveynext.data.session.DataChangeNotifier
import javax.inject.Inject
import javax.inject.Singleton

interface InterviewerRepository {
    suspend fun home(): InterviewerHomeModel

    /** @param state null lists surveys in every state */
    suspend fun surveys(page: Int, limit: Int, state: SurveyState?): PageModel<SurveyModel>

    suspend fun createSurveyForm(): CreateSurveyFormModel

    suspend fun regions(countryId: Int): List<RegionModel>

    suspend fun createSurvey(
        title: String, description: String, categoryId: Int, countryId: Int, regionId: Int, minutes: Int
    ): SurveyModel

    suspend fun survey(id: Int): SurveyModel

    suspend fun editSurveyInfo(id: Int, title: String, description: String, minutes: Int, categoryId: Int)

    suspend fun deleteSurvey(id: Int)

    suspend fun publishSurvey(id: Int, expectedAnswers: Int, pointsPerAnswer: Int)

    suspend fun setPaused(id: Int, paused: Boolean)

    suspend fun analytics(id: Int): AnalyticsModel

    suspend fun addQuestion(
        surveyId: Int, text: String, type: QuestionType, allowMultiAnswer: Boolean, options: List<String>
    ): QuestionModel

    /** @param options null leaves the existing choices untouched */
    suspend fun editQuestion(surveyId: Int, questionId: Int, text: String, options: List<String>?)

    suspend fun deleteQuestion(surveyId: Int, questionId: Int)
}

@Singleton
class InterviewerRepositoryImpl @Inject constructor(
    private val remote: InterviewerRemoteDataSource,
    private val restClient: RestClient,
    private val notifier: DataChangeNotifier,
) : InterviewerRepository {

    private var cachedForm: CreateSurveyFormModel? = null

    override suspend fun home() = remote.home().requireData("Couldn't load your dashboard")

    override suspend fun surveys(page: Int, limit: Int, state: SurveyState?) =
        remote.listSurveys(page, limit, state?.value).requireData("Couldn't load surveys")

    override suspend fun createSurveyForm(): CreateSurveyFormModel {
        cachedForm?.let { return it }
        return remote.createSurveyForm().requireData("Couldn't load the survey form")
            .also { cachedForm = it }
    }

    override suspend fun regions(countryId: Int) =
        restClient.getRegions(countryId).requireData("Couldn't load regions")

    override suspend fun createSurvey(
        title: String, description: String, categoryId: Int, countryId: Int, regionId: Int, minutes: Int
    ): SurveyModel {
        val request = CreateSurveyRequestModel(
            title = title,
            description = description,
            categoryId = categoryId,
            countryId = countryId,
            regionId = regionId,
            minutes = minutes,
        )
        return remote.createSurvey(request).requireData("Couldn't create the survey")
            .also { notifier.notifyChanged() }
    }

    override suspend fun survey(id: Int) = remote.getSurvey(id).requireData("Couldn't load the survey")

    override suspend fun editSurveyInfo(id: Int, title: String, description: String, minutes: Int, categoryId: Int) {
        val request = EditSurveyInfoRequestModel(title, description, minutes, categoryId)
        remote.editSurveyInfo(id, request).requireSuccess("Couldn't save the survey")
        notifier.notifyChanged()
    }

    override suspend fun deleteSurvey(id: Int) {
        remote.deleteSurvey(id).requireSuccess("Couldn't delete the survey")
        notifier.notifyChanged()
    }

    override suspend fun publishSurvey(id: Int, expectedAnswers: Int, pointsPerAnswer: Int) {
        remote.publishSurvey(id, PublishSurveyRequestModel(expectedAnswers, pointsPerAnswer))
            .requireSuccess("Couldn't publish the survey")
        notifier.notifyChanged()
    }

    override suspend fun setPaused(id: Int, paused: Boolean) {
        remote.pauseSurvey(id, paused).requireSuccess("Couldn't update the survey")
        notifier.notifyChanged()
    }

    override suspend fun analytics(id: Int) = remote.analytics(id).requireData("Couldn't load analytics")

    override suspend fun addQuestion(
        surveyId: Int, text: String, type: QuestionType, allowMultiAnswer: Boolean, options: List<String>
    ): QuestionModel {
        val request = AddQuestionRequestModel(
            text = text,
            questionType = type.value,
            allowMultiAnswer = type == QuestionType.MultipleChoice && allowMultiAnswer,
            options = options.takeIf { type == QuestionType.MultipleChoice },
        )
        return remote.addQuestion(surveyId, request).requireData("Couldn't add the question")
            .also { notifier.notifyChanged() }
    }

    override suspend fun editQuestion(surveyId: Int, questionId: Int, text: String, options: List<String>?) {
        remote.editQuestion(surveyId, questionId, EditQuestionRequestModel(text, options))
            .requireSuccess("Couldn't save the question")
        notifier.notifyChanged()
    }

    override suspend fun deleteQuestion(surveyId: Int, questionId: Int) {
        remote.deleteQuestion(surveyId, questionId).requireSuccess("Couldn't delete the question")
        notifier.notifyChanged()
    }
}
