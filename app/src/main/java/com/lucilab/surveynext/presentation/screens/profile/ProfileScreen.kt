package com.lucilab.surveynext.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucilab.surveynext.data.model.Role
import com.lucilab.surveynext.data.model.UserModel
import com.lucilab.surveynext.presentation.common.formatPoints
import com.lucilab.surveynext.presentation.components.TabScaffold
import com.lucilab.surveynext.presentation.components.card.PointsCard
import com.lucilab.surveynext.presentation.components.dialog.ConfirmDialog
import com.lucilab.surveynext.presentation.screens.profile.viewmodel.ProfileViewModel
import com.lucilab.surveynext.presentation.theme.HeroEnd
import com.lucilab.surveynext.presentation.theme.HeroStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val user by viewModel.user.collectAsState()
    var confirmLogout by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    TabScaffold(title = "Account") { padding ->
        val current = user ?: return@TabScaffold
        PullToRefreshBox(
            isRefreshing = viewModel.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileHeader(current)

                if (current.userRole == Role.Admin) {
                    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.secondaryContainer) {
                        ListItem(
                            leadingContent = { Icon(Icons.Outlined.AdminPanelSettings, contentDescription = null) },
                            headlineContent = { Text("Admin account") },
                            supportingContent = {
                                Text("Platform administration isn't available in the app. Sign in as an interviewer or respondent.")
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                } else {
                    PointsCard(
                        label = if (current.userRole == Role.Interviewer) "Available balance" else "Points earned",
                        points = current.points,
                        caption = current.pendingPoints.takeIf { it > 0 }?.let { "${formatPoints(it)} pts pending" }
                    )
                }

                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Column {
                        InfoItem(Icons.Outlined.Email, "Email", current.email)
                        HorizontalDivider(Modifier.padding(start = 56.dp))
                        InfoItem(Icons.Outlined.Badge, "Role", current.userRole.label)
                        HorizontalDivider(Modifier.padding(start = 56.dp))
                        InfoItem(Icons.Outlined.Place, "Location", viewModel.location ?: "—")
                    }
                }

                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Column {
                        ActionItem(Icons.Outlined.Info, "About Survey Next", onClick = { showAbout = true })
                        HorizontalDivider(Modifier.padding(start = 56.dp))
                        ActionItem(
                            Icons.AutoMirrored.Rounded.Logout,
                            "Log out",
                            tint = MaterialTheme.colorScheme.error,
                            onClick = { confirmLogout = true }
                        )
                    }
                }
            }
        }
    }

    if (confirmLogout) {
        ConfirmDialog(
            title = "Log out?",
            message = "You'll need to sign in again to use Survey Next.",
            confirmLabel = "Log out",
            destructive = true,
            onConfirm = {
                confirmLogout = false
                onLogout()
            },
            onDismiss = { confirmLogout = false }
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
            title = { Text("Survey Next") },
            text = {
                Text(
                    "Interviewers publish surveys with a points reward. Respondents in the targeted region " +
                        "answer them and earn those points."
                )
            },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("OK") } }
        )
    }
}

@Composable
private fun ProfileHeader(user: UserModel) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            Modifier
                .size(88.dp)
                .background(Brush.linearGradient(listOf(HeroStart, HeroEnd)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(initials(user.name), style = MaterialTheme.typography.headlineMedium, color = Color.White)
        }
        Text(user.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
        Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InfoItem(icon: ImageVector, label: String, value: String) {
    ListItem(
        leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        overlineContent = { Text(label) },
        headlineContent = { Text(value) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(onClick = onClick, color = Color.Transparent, shape = MaterialTheme.shapes.large) {
        ListItem(
            leadingContent = { Icon(icon, contentDescription = null, tint = tint) },
            headlineContent = { Text(label, color = tint) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

private fun initials(name: String): String =
    name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }
