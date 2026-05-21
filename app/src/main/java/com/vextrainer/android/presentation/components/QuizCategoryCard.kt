package com.vextrainer.android.presentation.components

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

/**
 * @param isExpanded
 *   - null  → leaf node (subcategory): shows a forward arrow, navigates on tap
 *   - false → collapsed parent: shows ExpandMore chevron, toggles on tap
 *   - true  → expanded parent: shows ExpandLess chevron, toggles on tap
 */
@Composable
fun QuizCategoryCard(
    category: QuizCategory,
    onClick: () -> Unit,
    isExpanded: Boolean?,
    modifier: Modifier = Modifier
) {
    val isParent = isExpanded != null

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isParent) 2.dp else 1.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isParent)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = if (isParent) 10.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text      = category.categoryName,
                    style     = if (isParent)
                        MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    else
                        MaterialTheme.typography.bodyMedium,
                    color     = if (isParent)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                category.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text     = desc,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Show subcategory count on collapsed parent only
                if (isParent && isExpanded == false && category.subcategories.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text  = stringResource(R.string.quiz_count, category.subcategories.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Icon(
                imageVector = when (isExpanded) {
                    true  -> Icons.Default.ExpandLess
                    false -> Icons.Default.ExpandMore
                    null  -> Icons.AutoMirrored.Filled.ArrowForward
                },
                contentDescription = when (isExpanded) {
                    true  -> stringResource(R.string.cd_collapse)
                    false -> stringResource(R.string.cd_expand)
                    null  -> stringResource(R.string.cd_navigate_forward)
                },
                tint     = if (isParent)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(20.dp)
            )
        }
    }
}
