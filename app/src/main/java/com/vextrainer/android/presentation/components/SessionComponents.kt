package com.vextrainer.android.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.quiz.QuestionResult

//  OptionState 

enum class OptionState {
    DEFAULT,    // Not selected, answer not yet submitted
    SELECTED,   // User tapped this, answer not yet submitted
    CORRECT,    // This was the correct answer (revealed after submit)
    INCORRECT,  // User selected this but it was wrong
    MISSED      // Correct answer user did not select (multi-answer)
}

//  AnswerOptionButton 

@Composable
fun AnswerOptionButton(
    label: String,
    text: String,
    state: OptionState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = when (state) {
            OptionState.DEFAULT   -> MaterialTheme.colorScheme.surface
            OptionState.SELECTED  -> MaterialTheme.colorScheme.primaryContainer
            OptionState.CORRECT   -> Color(0xFFE8F5E9)
            OptionState.INCORRECT -> Color(0xFFFFEBEE)
            OptionState.MISSED    -> Color(0xFFFFF9C4)
        },
        animationSpec = tween(200),
        label = "optionColor"
    )

    val borderColor = when (state) {
        OptionState.DEFAULT   -> MaterialTheme.colorScheme.outline
        OptionState.SELECTED  -> MaterialTheme.colorScheme.primary
        OptionState.CORRECT   -> Color(0xFF2E7D32)
        OptionState.INCORRECT -> Color(0xFFB71C1C)
        OptionState.MISSED    -> Color(0xFFF57F17)
    }

    val textColor = when (state) {
        OptionState.DEFAULT   -> MaterialTheme.colorScheme.onSurface
        OptionState.SELECTED  -> MaterialTheme.colorScheme.onPrimaryContainer
        OptionState.CORRECT   -> Color(0xFF1B5E20)
        OptionState.INCORRECT -> Color(0xFF7F0000)
        OptionState.MISSED    -> Color(0xFFE65100)
    }

    OutlinedButton(
        onClick  = onClick,
        enabled  = enabled,
        modifier = modifier.fillMaxWidth(),   // height removed — MarkdownText needs to wrap
        shape    = RoundedCornerShape(12.dp),
        border   = BorderStroke(
            width = if (state != OptionState.DEFAULT) 2.dp else 1.dp,
            color = if (enabled || state == OptionState.DEFAULT) borderColor
                    else borderColor.copy(alpha = 0.5f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor         = containerColor,
            disabledContainerColor = containerColor
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            //  Option letter badge (A, B, C …) 
            Surface(
                shape    = CircleShape,
                color    = borderColor.copy(
                    alpha = if (state == OptionState.DEFAULT) 0.15f else 0.25f
                ),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text       = label,
                        style      = MaterialTheme.typography.labelLarge,
                        color      = textColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Answer text: inline markdown via AnnotatedString
            // Uses inlineMarkdown() instead of MarkdownText (AndroidView) so
            // that touches reach the OutlinedButton click handler correctly.
            Text(
                text     = inlineMarkdown(text),
                style    = MaterialTheme.typography.bodyLarge,
                color    = textColor,
                modifier = Modifier.weight(1f)
            )

            //  Result icon 
            when (state) {
                OptionState.CORRECT -> {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.CheckCircle, contentDescription = null,
                         tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                }
                OptionState.INCORRECT -> {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.Cancel, contentDescription = null,
                         tint = Color(0xFFB71C1C), modifier = Modifier.size(20.dp))
                }
                else -> {}
            }
        }
    }
}

//  QuizProgressBar 

@Composable
fun QuizProgressBar(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text  = stringResource(R.string.quiz_question_progress, current, total),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress  = { if (total > 0) current.toFloat() / total.toFloat() else 0f },
            modifier  = Modifier.fillMaxWidth().height(6.dp),
            color     = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

//  ScoreBadge 

@Composable
fun ScoreBadge(
    score: Double,
    passingScore: Double?,
    modifier: Modifier = Modifier
) {
    val passed  = passingScore == null || score >= passingScore
    val color   = if (passed) Color(0xFF2E7D32) else Color(0xFFB71C1C)
    val bgColor = if (passed) Color(0xFFE8F5E9)  else Color(0xFFFFEBEE)

    Surface(
        shape    = CircleShape,
        color    = bgColor,
        border   = BorderStroke(2.dp, color),
        modifier = modifier.size(140.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Number and % on the same row, % baseline-aligned to bottom of number
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text       = "%.0f".format(score),
                    fontSize   = 52.sp,
                    fontWeight = FontWeight.Bold,
                    color      = color,
                    lineHeight = 52.sp
                )
                Text(
                    text  = "%",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = color,
                    modifier   = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}

// PassFailChip: kept for source compat but no longer used on result screen

@Composable
fun PassFailChip(
    passed: Boolean,
    modifier: Modifier = Modifier
) {
    val color   = if (passed) Color(0xFF2E7D32) else Color(0xFFB71C1C)
    val bgColor = if (passed) Color(0xFFE8F5E9)  else Color(0xFFFFEBEE)
    val label   = if (passed) stringResource(R.string.quiz_passed)
                  else stringResource(R.string.quiz_failed)

    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = bgColor,
        border   = BorderStroke(1.dp, color),
        modifier = modifier
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelLarge,
            color      = color,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

//  QuestionResultRow 

@Composable
fun QuestionResultRow(
    result: QuestionResult,
    index: Int,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Three states: correct (green), wrong (orange), unanswered (grey)
    // userAnswerJson == null means the question was never reached/answered.
    val isUnanswered = result.userAnswerJson == null
    val onCardColor = Color(0xFF1A1A1A)

    val cardColor = when {
        isUnanswered     -> Color(0xFFF5F5F5)   // grey  — not answered
        result.isCorrect -> Color(0xFFF1F8E9)   // green — correct
        else             -> Color(0xFFFFF3E0)   // orange — wrong
    }
    val iconVector = when {
        isUnanswered     -> Icons.Default.RemoveCircle
        result.isCorrect -> Icons.Default.CheckCircle
        else             -> Icons.Default.Cancel
    }
    val iconTint = when {
        isUnanswered     -> Color(0xFF9E9E9E)   // grey
        result.isCorrect -> Color(0xFF2E7D32)   // green
        else             -> Color(0xFFB71C1C)   // red
    }
    val iconCd = when {
        isUnanswered     -> stringResource(R.string.quiz_answer_not_attempted)
        result.isCorrect -> stringResource(R.string.quiz_answer_correct)
        else             -> stringResource(R.string.quiz_answer_incorrect)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            //  Collapsed header row 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector        = iconVector,
                    contentDescription = iconCd,
                    tint               = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text     = "Q${index + 1}. ${result.questionText}",
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = onCardColor,
                    modifier = Modifier.weight(1f),
                    maxLines = if (expanded) Int.MAX_VALUE else 2
                )
                if (!isUnanswered) {
                    IconButton(
                        onClick  = { expanded = !expanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector        = if (expanded) Icons.Default.ExpandLess
                            else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) stringResource(R.string.cd_collapse)
                            else stringResource(R.string.cd_expand),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            //  Unanswered label 
            if (isUnanswered) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = stringResource(R.string.quiz_answer_not_attempted),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            }

            //  Expanded detail 
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                Spacer(Modifier.height(8.dp))

                result.explanation?.let { exp ->
                    Text(
                        text  = stringResource(R.string.quiz_explanation_label),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = onCardColor
                    )
                    Spacer(Modifier.height(4.dp))
                    // inlineMarkdown keeps this inside the Card's click area safely.
                    Text(
                        text  = inlineMarkdown(exp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = onCardColor
                    )
                }
            }
        }
    }
}
