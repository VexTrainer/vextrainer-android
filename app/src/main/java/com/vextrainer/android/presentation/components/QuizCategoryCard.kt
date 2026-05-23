package com.vextrainer.android.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.quiz.QuizCategory

@Composable
fun QuizCategoryCard(
    category: QuizCategory,
    onClick: () -> Unit,
    isExpanded: Boolean?,
    modifier: Modifier = Modifier
) {
    val isParent = isExpanded != null

    if (isParent) {
        // ── Parent category — ElevatedCard with chevron ───────────────────
        ElevatedCard(
            onClick   = onClick,
            modifier  = modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            colors    = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier          = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = inlineMarkdown(category.categoryName),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    category.description?.takeIf { it.isNotBlank() }?.let { desc ->
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text     = inlineMarkdown(desc),
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (isExpanded == false && category.subcategories.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text  = stringResource(
                                R.string.quiz_count, category.subcategories.size
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Icon(
                    imageVector        = if (isExpanded == true) Icons.Default.ExpandLess
                                         else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded == true)
                        stringResource(R.string.cd_collapse)
                    else
                        stringResource(R.string.cd_expand),
                    tint     = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(start = 8.dp).size(20.dp)
                )
            }
        }
    } else {
        // ── Leaf (subcategory) — plain clickable Row, no card wrapper ─────
        // Using a Row instead of ElevatedCard removes the implicit card
        // padding that was causing large gaps between subcategory items.
        Column(modifier = modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = inlineMarkdown(category.categoryName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    category.description?.takeIf { it.isNotBlank() }?.let { desc ->
                        Text(
                            text     = inlineMarkdown(desc),
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.cd_navigate_forward),
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.padding(start = 8.dp).size(18.dp)
                )
            }
            HorizontalDivider(
                color     = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )
        }
    }
}
