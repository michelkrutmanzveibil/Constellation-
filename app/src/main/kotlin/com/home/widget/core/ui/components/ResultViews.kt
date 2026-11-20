package com.home.widget.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.home.widget.core.provider.ResultAction
import com.home.widget.core.provider.ResultCard
import com.home.widget.core.provider.ResultItem

/** A compact top-5 row: title, subtitle, an optional badge, and up to two actions. */
@Composable
fun ResultItemRow(item: ResultItem, onAction: (String) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitleLine = listOfNotNull(item.subtitle, item.badge).joinToString("  ·  ")
                if (subtitleLine.isNotBlank()) {
                    Text(
                        text = subtitleLine,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            ActionButtons(actions = item.actions, onAction = onAction)
        }
    }
}

@Composable
fun SingleResultCardView(card: ResultCard, onAction: (String) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = card.title, style = MaterialTheme.typography.titleMedium)
            card.subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (card.details.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    card.details.forEach { detail ->
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(detail.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(detail.value, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                ActionButtons(actions = card.actions, onAction = onAction)
            }
        }
    }
}

@Composable
fun AnswerCardView(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun ActionButtons(actions: List<ResultAction>, onAction: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.forEach { action ->
            if (action.isPrimary) {
                Button(onClick = { onAction(action.id) }) { Text(action.label) }
            } else {
                OutlinedButton(onClick = { onAction(action.id) }) { Text(action.label) }
            }
        }
    }
}
