package com.lucilab.surveynext.data.repository

import com.lucilab.surveynext.data.model.AnswerDetailsModel
import com.lucilab.surveynext.data.model.AnswerModel
import com.lucilab.surveynext.data.model.PageModel
import com.lucilab.surveynext.data.model.RespondentHomeModel
import com.lucilab.surveynext.data.model.SurveyModel
import com.lucilab.surveynext.data.model.requireData
import com.lucilab.surveynext.data.model.requireSuccess
import com.lucilab.surveynext.data.service.datasource.RespondentRemoteDataSource
import com.lucilab.surveynext.data.service.requestModel.SubmitAnswerRequestModel
import com.lucilab.surveynext.data.service.requestModel.UserAnswerRequestModel
import com.lucilab.surveynext.data.session.DataChangeNotifier
import javax.inject.Inject
import javax.inject.Singleton

interface RespondentRepository {
    suspend fun home(): RespondentHomeModel

    suspend fun availableSurveys(page: Int, limit: Int): PageModel<SurveyModel>

    suspend fun survey(id: Int): SurveyModel

    /** @param answers responses keyed by question id; every question must be present */
    suspend fun submitAnswers(surveyId: Int, answers: Map<Int, List<String>>): AnswerModel

    suspend fun savedSurveys(): List<SurveyModel>

    suspend fun savedSurveyIds(): Set<Int>

    suspend fun saveSurvey(id: Int)

    suspend fun removeSavedSurvey(id: Int)

    suspend fun completedSurveys(page: Int, limit: Int): PageModel<SurveyModel>

    suspend fun answerDetails(surveyId: Int): AnswerDetailsModel
}

@Singleton
class RespondentRepositoryImpl @Inject constructor(
    private val remote: RespondentRemoteDataSource,
    private val notifier: DataChangeNotifier,
) : RespondentRepository {

    override suspend fun home() = remote.home().requireData("Couldn't load your dashboard")

    override suspend fun availableSurveys(page: Int, limit: Int) =
        remote.availableSurveys(page, limit).requireData("Couldn't load surveys")

    override suspend fun survey(id: Int) = remote.surveyDetail(id).requireData("Couldn't load the survey")

    override suspend fun submitAnswers(surveyId: Int, answers: Map<Int, List<String>>): AnswerModel {
        val request = SubmitAnswerRequestModel(
            answers.map { (questionId, responses) -> UserAnswerRequestModel(questionId, responses) }
        )
        return remote.submitAnswer(surveyId, request).requireData("Couldn't submit your answers")
            .also { notifier.notifyChanged() }
    }

    override suspend fun savedSurveys() = remote.savedSurveys().requireData("Couldn't load saved surveys")

    override suspend fun savedSurveyIds() =
        remote.savedSurveyIds().requireData("Couldn't load saved surveys").toSet()

    override suspend fun saveSurvey(id: Int) {
        remote.saveSurvey(id).requireSuccess("Couldn't save the survey")
        notifier.notifyChanged()
    }

    override suspend fun removeSavedSurvey(id: Int) {
        remote.removeSavedSurvey(id).requireSuccess("Couldn't remove the survey")
        notifier.notifyChanged()
    }

    override suspend fun completedSurveys(page: Int, limit: Int) =
        remote.completed(page, limit).requireData("Couldn't load your history")

    override suspend fun answerDetails(surveyId: Int) =
        remote.answerDetails(surveyId).requireData("Couldn't load your answers")
}
