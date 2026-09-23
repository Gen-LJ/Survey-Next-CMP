package com.lucilab.surveynext.presentation.screens.respondent.discover.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucilab.surveynext.data.model.SurveyModel
import com.lucilab.surveynext.data.repository.RespondentRepository
import com.lucilab.surveynext.data.session.DataChangeNotifier
import com.lucilab.surveynext.presentation.common.LoadState
import com.lucilab.surveynext.presentation.common.PAGE_SIZE
import com.lucilab.surveynext.presentation.common.PagedList
import com.lucilab.surveynext.presentation.common.toLoadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

const val MAX_SAVED = 5

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val repository: RespondentRepository,
    notifier: DataChangeNotifier,
) : ViewModel() {

    var showingSaved by mutableStateOf(false)
        private set

    var available by mutableStateOf(PagedList<SurveyModel>())
        private set

    var saved by mutableStateOf<LoadState<List<SurveyModel>>>(LoadState.Loading)
        private set

    var savedIds by mutableStateOf<Set<Int>>(emptySet())
        private set

    var message by mutableStateOf<String?>(null)
        private set

    private var loadJob: Job? = null
    private val pendingToggles = mutableSetOf<Int>()

    init {
        viewModelScope.launch {
            notifier.version.collect {
                reloadAvailable(showRefreshing = false)
                loadSaved()
            }
        }
    }

    fun showSaved(saved: Boolean) {
        showingSaved = saved
    }

    fun clearMessage() {
        message = null
    }

    fun refresh() {
        reloadAvailable(showRefreshing = true)
        viewModelScope.launch { loadSaved() }
    }

    fun loadMore() {
        val list = available
        if (!list.hasNext || list.isLoadingMore || loadJob?.isActive == true || list.error != null) return
        available = list.copy(isLoadingMore = true)
        loadJob = viewModelScope.launch { fetchAvailable(list.page + 1) }
    }

    fun retryAvailable() {
        available = available.copy(error = null)
        if (available.items.isEmpty()) reloadAvailable(showRefreshing = false) else loadMore()
    }

    fun retrySaved() {
        saved = LoadState.Loading
        viewModelScope.launch { loadSaved() }
    }

    fun toggleSaved(survey: SurveyModel) {
        if (!pendingToggles.add(survey.id)) return
        val wasSaved = survey.id in savedIds
        if (!wasSaved && savedIds.size >= MAX_SAVED) {
            pendingToggles.remove(survey.id)
            message = "You can save up to $MAX_SAVED surveys. Answer or remove one first."
            return
        }
        // Optimistic; the change notification then reloads both lists.
        savedIds = if (wasSaved) savedIds - survey.id else savedIds + survey.id
        viewModelScope.launch {
            runCatching {
                if (wasSaved) repository.removeSavedSurvey(survey.id) else repository.saveSurvey(survey.id)
            }.onSuccess {
                message = if (wasSaved) "Removed from saved" else "Saved for later"
            }.onFailure {
                savedIds = if (wasSaved) savedIds + survey.id else savedIds - survey.id
                message = it.message
            }
            pendingToggles.remove(survey.id)
        }
    }

    private suspend fun loadSaved() {
        val result = runCatching { repository.savedSurveys() }
        result.onSuccess { list -> savedIds = list.map { it.id }.toSet() }
        if (result.isSuccess || saved !is LoadState.Success) saved = result.toLoadState()
    }

    private fun reloadAvailable(showRefreshing: Boolean) {
        loadJob?.cancel()
        available = available.copy(isRefreshing = showRefreshing, isLoadingMore = false, error = null)
        loadJob = viewModelScope.launch { fetchAvailable(1) }
    }

    private suspend fun fetchAvailable(page: Int) {
        runCatching { repository.availableSurveys(page, PAGE_SIZE) }
            .onSuccess { result ->
                available = available.copy(
                    items = if (page == 1) result.items else (available.items + result.items).distinctBy { it.id },
                    page = page,
                    hasNext = result.meta.hasNext,
                    isRefreshing = false,
                    isLoadingMore = false,
                    error = null
                )
            }
            .onFailure {
                available = available.copy(
                    isRefreshing = false,
                    isLoadingMore = false,
                    error = it.message ?: "Couldn't load surveys"
                )
            }
    }
}
