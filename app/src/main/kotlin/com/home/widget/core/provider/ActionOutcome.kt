package com.home.widget.core.provider

/** What the widget should do after [Provider.performAction] returns. */
sealed interface ActionOutcome {
    /** The action completed inline (e.g. playback started); collapse the widget. */
    data object Collapse : ActionOutcome

    /** The action necessarily left Home to hand off to the provider's app. */
    data class LaunchedFullApp(val packageName: String?) : ActionOutcome

    /** The action failed; keep the card open and surface a short reason. */
    data class Failed(val reason: String) : ActionOutcome
}
