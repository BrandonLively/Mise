package com.patchfox.mise.ui.screen.login

import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patchfox.mise.ui.theme.MiseTokens

@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val activity = LocalContext.current as? Activity

    LaunchedEffect(state.signedIn) {
        if (state.signedIn) onSignedIn()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiseTokens.colors.bg)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        Spacer(Modifier.height(24.dp))
        ColorStrip()
        Spacer(Modifier.height(64.dp))
        Column(modifier = Modifier.padding(horizontal = 36.dp)) {
            Text("WELCOME BACK, CHEF.", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "mise",
                    style = MiseTokens.text.display.copy(fontSize = androidx.compose.ui.unit.TextUnit.Unspecified),
                    color = MiseTokens.colors.ink,
                )
                Text(".", style = MiseTokens.text.display, color = MiseTokens.colors.accent)
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Sunday cook · Monday → Sunday eat. Open a cook card, walk the swipeable plan, weigh the result.",
                style = MiseTokens.text.body,
                color = MiseTokens.colors.ink2,
            )
        }

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.errorMessage != null) {
                Text(
                    state.errorMessage!!,
                    style = MiseTokens.text.small,
                    color = MiseTokens.colors.accent,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(MiseTokens.colors.surface, RoundedCornerShape(999.dp))
                    .clickable(enabled = !state.signingIn && activity != null) {
                        activity?.let { viewModel.signIn(it) }
                    }
                    .padding(horizontal = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(MiseTokens.colors.bg, RoundedCornerShape(999.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text("G", style = MiseTokens.text.bodyEmphasis, color = MiseTokens.colors.ink) }
                Spacer(Modifier.weight(1f))
                if (state.signingIn) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Continue with Google", style = MiseTokens.text.bodyEmphasis, color = MiseTokens.colors.ink)
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Filled.KeyboardArrowRight, null, tint = MiseTokens.colors.ink3)
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("NO ACCOUNT REQUIRED · PERSONAL USE", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
                Spacer(Modifier.width(10.dp))
                Text("↗ HELP", style = MiseTokens.text.micro, color = MiseTokens.colors.accent)
            }
        }
    }
}

@Composable
private fun ColorStrip() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp)
            .height(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(
            MiseTokens.colors.blue,
            MiseTokens.colors.green,
            MiseTokens.colors.purple,
            MiseTokens.colors.yellow,
        ).forEach { color ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .background(color, RoundedCornerShape(3.dp)),
            )
        }
    }
}
