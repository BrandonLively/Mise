package com.patchfox.mise.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.patchfox.mise.domain.model.PrepTaskId
import com.patchfox.mise.domain.model.Recipe
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.domain.usecase.PrepTask
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.theme.recipe

@Composable
fun PrepWorkList(
    tasks: List<PrepTask>,
    checked: Set<PrepTaskId>,
    recipesById: Map<RecipeId, Recipe>,
    onToggle: (PrepTaskId, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("PREP WORK · BEFORE SUNDAY", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
            Spacer(Modifier.weight(1f))
            Text(
                "${checked.count { id -> tasks.any { it.id == id } }}/${tasks.size} DONE",
                style = MiseTokens.text.micro,
                color = MiseTokens.colors.ink3,
            )
        }
        Spacer(Modifier.height(8.dp))
        MiseCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                tasks.forEachIndexed { idx, task ->
                    val isChecked = task.id in checked
                    val recipeColor = task.attributedRecipeId?.let { recipesById[it]?.color }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle(task.id, !isChecked) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val checkShape = RoundedCornerShape(4.dp)
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    if (isChecked) MiseTokens.colors.ink else MiseTokens.colors.surface,
                                    checkShape,
                                )
                                .border(
                                    width = if (isChecked) 0.dp else 1.5.dp,
                                    color = if (isChecked) MiseTokens.colors.ink else MiseTokens.colors.ink3,
                                    shape = checkShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isChecked) {
                                Icon(
                                    Icons.Filled.Check,
                                    null,
                                    tint = MiseTokens.colors.surface,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                        Spacer(Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    task.dayShortCaps,
                                    style = MiseTokens.text.micro,
                                    color = MiseTokens.colors.ink3,
                                )
                                if (recipeColor != null) {
                                    Spacer(Modifier.size(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(MiseTokens.colors.recipe(recipeColor), CircleShape),
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                task.text,
                                style = MiseTokens.text.bodyEmphasis.copy(
                                    textDecoration = if (isChecked) TextDecoration.LineThrough else null,
                                ),
                                color = if (isChecked) MiseTokens.colors.ink3 else MiseTokens.colors.ink,
                            )
                        }
                    }
                    if (idx < tasks.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MiseTokens.colors.line),
                        )
                    }
                }
                if (tasks.isEmpty()) {
                    Text(
                        "Nothing to prep before Sunday.",
                        style = MiseTokens.text.small,
                        color = MiseTokens.colors.ink3,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}
