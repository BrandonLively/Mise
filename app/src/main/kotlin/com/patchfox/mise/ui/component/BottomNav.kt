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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.patchfox.mise.ui.nav.MiseDestination
import com.patchfox.mise.ui.theme.MiseTokens

data class NavItem(val destination: MiseDestination, val label: String, val icon: ImageVector)

val MiseNavItems = listOf(
    NavItem(MiseDestination.Home, "Schedule", Icons.Filled.CalendarMonth),
    NavItem(MiseDestination.Cook, "Cook", Icons.Filled.Restaurant),
    NavItem(MiseDestination.Recipes, "Recipes", Icons.Filled.MenuBook),
)

@Composable
fun BottomNav(
    selected: MiseDestination,
    onSelect: (MiseDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MiseTokens.colors.surface)
            .border(0.dp, MiseTokens.colors.line),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 22.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MiseNavItems.forEach { item ->
                val active = item.destination.matches(selected)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelect(item.destination) }
                        .padding(vertical = 6.dp, horizontal = 12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (active) MiseTokens.colors.accentWash else MiseTokens.colors.surface,
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = item.label,
                            tint = if (active) MiseTokens.colors.ink else MiseTokens.colors.ink3,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        item.label,
                        style = MiseTokens.text.micro,
                        color = if (active) MiseTokens.colors.ink else MiseTokens.colors.ink3,
                    )
                }
            }
        }
    }
}

@Composable
fun MiseNavRail(
    selected: MiseDestination,
    onSelect: (MiseDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MiseTokens.colors.surface)
            .padding(vertical = 24.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("m", style = MiseTokens.text.h2, color = MiseTokens.colors.accent)
        Spacer(Modifier.height(32.dp))
        MiseNavItems.forEach { item ->
            val active = item.destination.matches(selected)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .clickable { onSelect(item.destination) },
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (active) MiseTokens.colors.accentWash else MiseTokens.colors.surface,
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        tint = if (active) MiseTokens.colors.ink else MiseTokens.colors.ink3,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    item.label,
                    style = MiseTokens.text.micro,
                    color = if (active) MiseTokens.colors.ink else MiseTokens.colors.ink3,
                )
            }
        }
    }
}
