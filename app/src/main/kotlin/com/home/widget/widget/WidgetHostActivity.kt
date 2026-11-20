package com.home.widget.widget

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.home.widget.core.provider.ProviderRegistry
import com.home.widget.core.ui.components.ExpandPanel
import com.home.widget.core.ui.components.ProviderIconRow
import com.home.widget.core.ui.components.SearchBarView
import com.home.widget.core.ui.theme.HomeTheme
import com.home.widget.viewmodel.WidgetViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * A trampoline activity docked over the widget's spot on the Home Screen.
 * RemoteViews can't run Compose, animate, call the network, or hold a real
 * text field, so this activity is what actually performs the "expand
 * downward" the widget appears to do — it is styled with no window chrome
 * so the transition from the real widget reads as continuous rather than as
 * launching a new screen. Finishing this activity is what "the widget
 * collapses" means in practice. See README.md "How the expand animation
 * really works".
 */
class WidgetHostActivity : ComponentActivity() {

    private val viewModel: WidgetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applyProviderExtra(intent)
        onBackPressedDispatcher.addCallback(this) { viewModel.collapse() }

        setContent {
            HomeTheme {
                LaunchedEffect(Unit) {
                    viewModel.closeEvents.collectLatest { finish() }
                }
                WidgetHostScreen(viewModel = viewModel, onDismiss = { viewModel.collapse() })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        applyProviderExtra(intent)
    }

    private fun applyProviderExtra(intent: Intent?) {
        val providerId = intent?.getStringExtra(EXTRA_PROVIDER_ID) ?: return
        if (ProviderRegistry.byId(providerId) != null) viewModel.onProviderTapped(providerId)
    }

    companion object {
        const val EXTRA_PROVIDER_ID = "provider_id"
    }
}

@Composable
private fun WidgetHostScreen(viewModel: WidgetViewModel, onDismiss: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val panelMaxHeight = remember(screenHeightDp) {
        val totalBudgetDp = screenHeightDp * 0.38f
        val headerHeightDp = 132f
        (totalBudgetDp - headerHeightDp).coerceAtLeast(120f).dp
    }

    // Outer box: tapping anywhere outside the card dismisses, like tapping
    // away from any Home Screen overlay.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
    ) {
        Column(
            // Inner column swallows its own taps so they don't bubble up and
            // dismiss the card the user is actually interacting with.
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 3.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val provider = state.selectedProviderId?.let { ProviderRegistry.byId(it) }

                    SearchBarView(
                        query = state.query,
                        onQueryChange = viewModel::onQueryChange,
                        onSubmit = { /* results already stream live as the user types */ },
                    )

                    ProviderIconRow(
                        providers = ProviderRegistry.providers,
                        selectedProviderId = state.selectedProviderId,
                        onProviderClick = { viewModel.onProviderTapped(it.id) },
                        modifier = Modifier.padding(top = 8.dp),
                    )

                    if (provider != null) {
                        ExpandPanel(
                            provider = provider,
                            result = state.result,
                            isLoading = state.isLoading,
                            maxHeight = panelMaxHeight,
                            onAction = { actionId -> viewModel.onAction(provider.id, actionId) },
                            onOpenFullApp = { viewModel.onOpenFullApp(provider.id) },
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                }
            }
        }
    }
}
