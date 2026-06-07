package com.vextrainer.android.presentation.ui.quiz.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vextrainer.android.R
import com.vextrainer.android.presentation.components.ErrorCard
import com.vextrainer.android.presentation.components.LoadingOverlay
import com.vextrainer.android.presentation.components.QuizCategoryCard
import com.vextrainer.android.presentation.components.VexTopAppBar

@Composable
fun QuizCategoryScreen(
    onCategoryClick: (categoryId: Int, categoryName: String) -> Unit,
    onHistoryClick: () -> Unit,
    onHomeClick: () -> Unit,
    viewModel: QuizCategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            VexTopAppBar(
                title       = stringResource(R.string.quiz_categories_title),
                onLogoClick = onHomeClick,
                actions     = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(
                            imageVector        = Icons.Default.History,
                            contentDescription = stringResource(R.string.cd_history)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> LoadingOverlay()

                uiState.error != null -> ErrorCard(
                    message = uiState.error!!.asString(),
                    onRetry = viewModel::loadCategories
                )

                uiState.flatItems.isEmpty() -> Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text      = stringResource(R.string.quiz_categories_empty),
                        textAlign = TextAlign.Center
                    )
                }

                else -> CategoryList(
                    heading         = stringResource(R.string.quiz_categories_title),
                    flatItems       = uiState.flatItems,
                    onToggle        = viewModel::toggleCategory,
                    onCategoryClick = onCategoryClick
                )
            }
        }
    }
}

@Composable
private fun CategoryList(
    heading: String,
    flatItems: List<CategoryListItem>,
    onToggle: (categoryId: Int) -> Unit,
    onCategoryClick: (categoryId: Int, categoryName: String) -> Unit
) {
    LazyColumn(
        contentPadding      = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item(key = "heading") {
            Text(
                text       = heading,
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface,
                modifier   = Modifier.padding(bottom = 8.dp)
            )
        }

        // items() — LazyColumn knows the count upfront, only composes visible rows.
        // Stable keys prevent unnecessary recomposition on expand/collapse.
        items(
            items = flatItems,
            key   = { item ->
                when (item) {
                    is CategoryListItem.Parent -> item.category.categoryId
                    is CategoryListItem.Child  -> "child_${item.category.categoryId}"
                }
            }
        ) { item ->
            when (item) {
                is CategoryListItem.Parent -> QuizCategoryCard(
                    category   = item.category,
                    onClick    = {
                        if (item.category.subcategories.isNotEmpty())
                            onToggle(item.category.categoryId)
                        else
                            onCategoryClick(item.category.categoryId, item.category.categoryName)
                    },
                    isExpanded = if (item.category.subcategories.isEmpty()) null
                                 else item.isExpanded
                )
                is CategoryListItem.Child -> QuizCategoryCard(
                    category   = item.category,
                    onClick    = { onCategoryClick(item.category.categoryId, item.category.categoryName) },
                    isExpanded = null,
                    modifier   = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
