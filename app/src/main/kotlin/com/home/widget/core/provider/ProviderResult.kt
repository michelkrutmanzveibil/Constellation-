package com.home.widget.core.provider

/**
 * The generic shapes a snippet can take. This is the vocabulary the widget's
 * result views are built against — never a provider-specific type — which is
 * what keeps the widget itself provider-agnostic.
 */
sealed interface ProviderResult {
    /** A compact top-5 list. Never paginated; if there's more, the UI shows "Open App ->" instead. */
    data class TopFive(val items: List<ResultItem>) : ProviderResult

    /** A single-result snippet (e.g. a ride estimate). */
    data class SingleCard(val card: ResultCard) : ProviderResult

    /** A short text answer (e.g. ChatGPT). */
    data class Answer(val text: String, val truncated: Boolean) : ProviderResult

    /** Nothing meaningful came back for this query. */
    data object Empty : ProviderResult

    /**
     * The provider genuinely cannot answer inline right now (no API key
     * configured, the call failed, permission missing, ...). The UI degrades
     * to a single "Open App ->" affordance. [reason] is for logs/debugging,
     * never shown as a fabricated result.
     */
    data class Unavailable(val reason: String) : ProviderResult
}

data class ResultItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val badge: String? = null,
    val imageUrl: String? = null,
    val actions: List<ResultAction>,
)

data class ResultCard(
    val title: String,
    val subtitle: String? = null,
    val details: List<LabeledValue> = emptyList(),
    val actions: List<ResultAction>,
)

data class LabeledValue(val label: String, val value: String)

data class ResultAction(
    val id: String,
    val label: String,
    val isPrimary: Boolean = false,
)
