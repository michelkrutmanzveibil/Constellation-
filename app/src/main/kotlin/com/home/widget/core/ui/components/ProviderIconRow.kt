package com.home.widget.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.home.widget.R
import com.home.widget.core.provider.Provider

/** The row of five real, installed app icons beneath the search bar. */
@Composable
fun ProviderIconRow(
    providers: List<Provider>,
    selectedProviderId: String?,
    onProviderClick: (Provider) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        val context = LocalContext.current
        providers.forEach { provider ->
            val isSelected = provider.id == selectedProviderId
            val painter = rememberProviderIconPainter(context, provider)

            IconButton(
                onClick = { onProviderClick(provider) },
                modifier = Modifier.size(56.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                            shape = CircleShape,
                        )
                        .padding(6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (painter != null) {
                        Image(
                            painter = painter,
                            contentDescription = stringResource(R.string.cd_provider_icon, provider.displayName),
                            modifier = Modifier.size(32.dp),
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_get_app),
                            contentDescription = stringResource(R.string.cd_provider_icon, provider.displayName),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}
