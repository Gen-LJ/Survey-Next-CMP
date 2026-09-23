package com.lucilab.surveynext.presentation.screens.interviewer.surveys.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucilab.surveynext.data.model.SurveyModel
import com.lucilab.surveynext.data.model.SurveyState
import com.lucilab.surveynext.data.repository.InterviewerRepository
import com.lucilab.surveynext.data.session.DataChangeNotifier
import com.lucilab.surveynext.presentation.common.PAGE_SIZE
import com.lucilab.surveynext.presentation.common.PagedList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InterviewerSurveysViewModel @Inject constructor(
    private val repository: InterviewerRepository,
    notifier: DataChangeNotifier,
) : ViewModel() {

    /** Null shows every state. */
    var filter by mutableStateOf<SurveyState?>(null)
        private set

    var list by mutableStateOf(PagedList<SurveyModel>())
        private set

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            notifier.version.collect { reload(showRefreshing = false) }
        }
    }

    fun selectFilter(state: SurveyState?) {
        if (state == filter) return
        filter = state
        list = PagedList()
        reload(showRefreshing = false)
    }

    fun refresh() = reload(showRefreshing = true)

    fun loadMore() {
        if (!list.hasNext || list.isLoadingMore || loadJob?.isActive == true || list.error != null) return
        list = list.copy(isLoadingMore = true)
        loadJob = viewModelScope.launch { fetch(list.page + 1) }
    }

    fun retry() {
        list = list.copy(error = null)
        if (list.items.isEmpty()) reload(showRefreshing = false) else loadMore()
    }

    private fun reload(showRefreshing: Boolean) {
        loadJob?.cancel()
        list = list.copy(isRefreshing = showRefreshing, isLoadingMore = false, error = null)
        loadJob = viewModelScope.launch { fetch(1) }
    }

    private suspend fun fetch(page: Int) {
        runCatching { repository.surveys(page, PAGE_SIZE, filter) }
            .onSuccess { result ->
                list = list.copy(
                    items = if (page == 1) result.items else (list.items + result.items).distinctBy { it.id },
                    page = page,
                    hasNext = result.meta.hasNext,
                    isRefreshing = false,
                    isLoadingMore = false,
                    error = null
                )
            }
            .onFailure {
                list = list.copy(isRefreshing = false, isLoadingMore = false, error = it.message ?: "Couldn't load surveys")
            }
    }
}
