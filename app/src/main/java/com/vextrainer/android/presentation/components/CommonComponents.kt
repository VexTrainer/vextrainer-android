package com.vextrainer.android.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vextrainer.android.R

@Composable
fun LoadingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier         = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier         = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text      = message,
                style     = MaterialTheme.typography.bodyLarge,
                color     = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text(stringResource(R.string.action_retry))
            }
        }
    }
}

/**
 * Standard top app bar for VexTrainer.
 *
 * Always shows the app logo + "VexTrainer" brand in the title slot.
 * Tapping the logo/title calls [onLogoClick] — wire this to navigate home.
 *
 * The [onBack] parameter is kept for source compatibility with screens not yet
 * updated, but the back arrow is intentionally not rendered — Android's gesture
 * navigation and system back button handle back navigation.
 *
 * @param title       Screen title (used as accessibility label; not displayed
 *                    visually since the logo replaces the title slot).
 * @param onLogoClick Called when the user taps the logo/brand row. Pass a
 *                    lambda that navigates to the home screen. Pass null (or
 *                    omit) on the home screen itself so the tap is a no-op.
 * @param onBack      Retained for source compatibility — has no visual effect.
 * @param actions     Trailing action icons (History, Search, etc.).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VexTopAppBar(
    title: String,
    onLogoClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,   // kept for source compat; back arrow not shown
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = if (onLogoClick != null)
                    Modifier.clickable(onClick = onLogoClick)
                else
                    Modifier
            ) {
                Image(
                    painter            = painterResource(R.drawable.logo_vextrainer),
                    contentDescription = stringResource(R.string.app_name),
                    modifier           = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = "VexTrainer",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        navigationIcon = {},   // back arrow removed — Android handles back navigation
        actions        = actions,
        colors         = TopAppBarDefaults.topAppBarColors(
            containerColor             = MaterialTheme.colorScheme.primary,
            titleContentColor          = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor     = MaterialTheme.colorScheme.onPrimary
        )
    )
}
