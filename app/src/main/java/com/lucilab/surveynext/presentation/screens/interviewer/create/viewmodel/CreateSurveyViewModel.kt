package com.lucilab.surveynext.presentation.screens.interviewer.create.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucilab.surveynext.data.model.CategoryModel
import com.lucilab.surveynext.data.model.CountryModel
import com.lucilab.surveynext.data.model.RegionModel
import com.lucilab.surveynext.data.repository.InterviewerRepository
import com.lucilab.surveynext.data.session.SessionManager
import com.lucilab.surveynext.presentation.common.LoadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateSurveyFormState(
    val title: String = "",
    val description: String = "",
    val minutes: Int = 5,
    val category: CategoryModel? = null,
    val country: CountryModel? = null,
    val region: RegionModel? = null,
    val regions: List<RegionModel> = emptyList(),
    val regionsLoading: Boolean = false,

    val titleError: String? = null,
    val descriptionError: String? = null,
    val categoryError: String? = null,
    val countryError: String? = null,
    val regionError: String? = null,
)

data class CreateSurveyOptions(
    val categories: List<CategoryModel>,
    val countries: List<CountryModel>,
)

@HiltViewModel
class CreateSurveyViewModel @Inject constructor(
    private val repository: InterviewerRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    var options by mutableStateOf<LoadState<CreateSurveyOptions>>(LoadState.Loading)
        private set

    var form by mutableStateOf(CreateSurveyFormState())
        private set

    var isSubmitting by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    /** Set once the survey exists; the screen navigates to its editor. */
    var createdSurveyId by mutableStateOf<Int?>(null)
        private set

    private var regionsJob: Job? = null

    init {
        load()
    }

    fun load() {
        options = LoadState.Loading
        viewModelScope.launch {
            runCatching { repository.createSurveyForm() }
                .onSuccess { data ->
                    options = LoadState.Success(CreateSurveyOptions(data.categoryList, data.countryList))
                    // Default to targeting the author's own country and region.
                    val user = sessionManager.user.value
                    data.countryList.firstOrNull { it.id.toInt() == user?.countryId }
                        ?.let { onCountrySelected(it, preferredRegionId = user?.regionId) }
                }
                .onFailure { options = LoadState.Error(it.message ?: "Couldn't load the survey form") }
        }
    }

    fun onTitleChange(value: String) {
        form = form.copy(title = value, titleError = null)
    }

    fun onDescriptionChange(value: String) {
        form = form.copy(description = value, descriptionError = null)
    }

    fun onMinutesChange(value: Int) {
        form = form.copy(minutes = value.coerceIn(1, 60))
    }

    fun onCategorySelected(category: CategoryModel) {
        form = form.copy(category = category, categoryError = null)
    }

    fun onCountrySelected(country: CountryModel, preferredRegionId: Int? = null) {
        if (form.country?.id == country.id && preferredRegionId == null) return
        form = form.copy(country = country, region = null, regions = emptyList(), regionsLoading = true, countryError = null)
        regionsJob?.cancel()
        regionsJob = viewModelScope.launch {
            runCatching { repository.regions(country.id.toInt()) }
                .onSuccess { regions ->
                    form = form.copy(
                        regions = regions,
                        regionsLoading = false,
                        region = regions.firstOrNull { it.id.toInt() == preferredRegionId }
                    )
                }
                .onFailure {
                    form = form.copy(regionsLoading = false)
                    errorMessage = it.message
                }
        }
    }

    fun onRegionSelected(region: RegionModel) {
        form = form.copy(region = region, regionError = null)
    }

    fun clearError() {
        errorMessage = null
    }

    fun submit() {
        val validated = form.copy(
            titleError = if (form.title.isBlank()) "Give your survey a title" else null,
            descriptionError = if (form.description.isBlank()) "Tell respondents what it's about" else null,
            categoryError = if (form.category == null) "Pick a category" else null,
            countryError = if (form.country == null) "Pick a country" else null,
            regionError = if (form.region == null) "Pick a region" else null,
        )
        form = validated
        val category = validated.category ?: return
        val country = validated.country ?: return
        val region = validated.region ?: return
        if (validated.titleError != null || validated.descriptionError != null) return

        viewModelScope.launch {
            isSubmitting = true
            runCatching {
                repository.createSurvey(
                    title = validated.title.trim(),
                    description = validated.description.trim(),
                    categoryId = category.id,
                    countryId = country.id.toInt(),
                    regionId = region.id.toInt(),
                    minutes = validated.minutes,
                )
            }.onSuccess {
                createdSurveyId = it.id
            }.onFailure {
                errorMessage = it.message
            }
            isSubmitting = false
        }
    }
}
