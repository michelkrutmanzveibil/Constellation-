package com.home.widget.core.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.home.widget.R
import com.home.widget.core.provider.Provider
import com.home.widget.core.provider.ProviderResult

/**
 * The expanding region below the icon row. This is the widget "expanding
 * downward": [Modifier.animateContentSize] grows/shrinks the panel as the
 * result comes in, capped at [maxHeight] so the whole widget never exceeds
 * ~35-40% of the screen and never needs to scroll.
 */
@Composable
fun ExpandPanel(
    provider: Provider,
    result: ProviderResult?,
    isLoading: Boolean,
    maxHeight: Dp,
    onAction: (String) -> Unit,
    onOpenFullApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .animateContentSize(animationSpec = tween(180)),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when {
            isLoading -> LoadingRow()
            result == null -> Unit
            else -> {
                when (result) {
                    is ProviderResult.TopFive -> {
                        result.items.forEach { item -> ResultItemRow(item = item, onAction = onAction) }
                        OpenAppRow(provider, onOpenFullApp)
                    }
                    is ProviderResult.SingleCard -> {
                        SingleResultCardView(card = result.card, onAction = onAction)
                        OpenAppRow(provider, onOpenFullApp)
                    }
                    is ProviderResult.Answer -> {
                        AnswerCardView(text = result.text)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (result.truncated) {
                                TextButton(onClick = onOpenFullApp) { Text(stringResource(R.string.action_continue)) }
                            }
                            OpenAppRow(provider, onOpenFullApp)
                        }
                    }
                    is ProviderResult.Empty -> {
                        Text(
                            text = stringResource(R.string.widget_no_results),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OpenAppRow(provider, onOpenFullApp)
                    }
                    is ProviderResult.Unavailable -> OpenAppRow(provider, onOpenFullApp)
                }
            }
        }
    }
}

@Composable
private fun LoadingRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.padding(4.dp))
    }
}

@Composable
private fun OpenAppRow(provider: Provider, onClick: () -> Unit) {
    TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
        Text(stringResource(R.string.action_open_app, provider.displayName))
        Icon(
            painter = painterResource(R.drawable.ic_arrow_forward),
            contentDescription = null,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}
