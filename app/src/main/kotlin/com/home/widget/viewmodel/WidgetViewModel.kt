package com.home.widget.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.home.widget.core.provider.ActionOutcome
import com.home.widget.core.provider.ProviderRegistry
import com.home.widget.core.provider.ProviderResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WidgetUiState(
    val query: String = "",
    val selectedProviderId: String? = null,
    val isLoading: Boolean = false,
    val result: ProviderResult? = null,
    val actionError: String? = null,
)

/**
 * Owns the one piece of state the whole widget cares about: which provider
 * (if any) is expanded, and what its snippet currently shows. Only one
 * provider is ever expanded at a time — selecting another replaces it.
 */
class WidgetViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(WidgetUiState())
    val state: StateFlow<WidgetUiState> = _state.asStateFlow()

    /** Emits once whenever the widget should fully collapse back to the Home Screen. */
    private val _closeEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val closeEvents: SharedFlow<Unit> = _closeEvents.asSharedFlow()

    private var searchJob: Job? = null
    private var debounceJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _state.update { it.copy(query = newQuery, actionError = null) }
        val providerId = _state.value.selectedProviderId ?: return
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(300)
            runSearch(providerId)
        }
    }

    fun onProviderTapped(providerId: String) {
        if (_state.value.selectedProviderId == providerId) {
            collapse()
            return
        }
        _state.update { it.copy(selectedProviderId = providerId, actionError = null) }
        runSearch(providerId)
    }

    private fun runSearch(providerId: String) {
        val provider = ProviderRegistry.byId(providerId) ?: return
        val query = _state.value.query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isBlank()) {
                _state.update { it.copy(isLoading = false, result = ProviderResult.Empty) }
                return@launch
            }
            _state.update { it.copy(isLoading = true) }
            val result = provider.search(getApplication(), query)
            if (_state.value.selectedProviderId == providerId) {
                _state.update { it.copy(isLoading = false, result = result) }
            }
        }
    }

    fun collapse() {
        searchJob?.cancel()
        debounceJob?.cancel()
        _state.update { it.copy(selectedProviderId = null, result = null, isLoading = false, actionError = null) }
        _closeEvents.tryEmit(Unit)
    }

    fun onAction(providerId: String, actionId: String) {
        val provider = ProviderRegistry.byId(providerId) ?: return
        viewModelScope.launch {
            when (val outcome = provider.performAction(getApplication(), actionId, _state.value.query)) {
                is ActionOutcome.Collapse, is ActionOutcome.LaunchedFullApp -> collapse()
                is ActionOutcome.Failed -> _state.update { it.copy(actionError = outcome.reason) }
            }
        }
    }

    fun onOpenFullApp(providerId: String) {
        val provider = ProviderRegistry.byId(providerId) ?: return
        provider.openFullApp(getApplication(), _state.value.query)
        collapse()
    }
}
