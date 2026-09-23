package com.lucilab.surveynext.presentation.screens.auth.register.view
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lucilab.surveynext.data.model.Role
import com.lucilab.surveynext.presentation.components.button.custombutton.CustomButton
import com.lucilab.surveynext.presentation.components.textfield.PasswordTextField
import com.lucilab.surveynext.presentation.screens.auth.register.viewmodel.RegisterState
import com.lucilab.surveynext.presentation.screens.auth.register.viewmodel.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterIdleView(
    viewModel: RegisterViewModel,
    padding: PaddingValues,
    onLoginClick: () -> Unit
) {
    val state = viewModel.state as? RegisterState.Idle ?: return
    val formState = state.form
    var expanded by remember { mutableStateOf(false) }
    var regionExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("How will you use Survey Next?", style = MaterialTheme.typography.titleMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RoleOption(
                icon = Icons.Outlined.RateReview,
                title = "Respondent",
                subtitle = "Answer surveys and earn points",
                selected = formState.role == Role.Respondent,
                onClick = { viewModel.onRoleSelected(Role.Respondent) },
                modifier = Modifier.weight(1f)
            )
            RoleOption(
                icon = Icons.Outlined.Campaign,
                title = "Interviewer",
                subtitle = "Create surveys and collect insights",
                selected = formState.role == Role.Interviewer,
                onClick = { viewModel.onRoleSelected(Role.Interviewer) },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = formState.name ?: "",
            onValueChange = viewModel::onNameChanged,
            label = { Text("Name") },
            singleLine = true,
            isError = formState.nameError != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            supportingText = formState.nameError?.let { { Text(it) } }
        )

        OutlinedTextField(
            value = formState.email ?: "",
            onValueChange = viewModel::onEmailChanged,
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = formState.emailError != null,
            modifier = Modifier.fillMaxWidth(),
            supportingText = formState.emailError?.let { { Text(it) } }
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                isError = formState.countryError != null,
                value = formState.selectedCountry?.name ?: "",
                placeholder = { Text("Select country") },
                onValueChange = {},
                readOnly = true,
                label = { Text("Country") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth(),
                supportingText = formState.countryError?.let { { Text(it) } }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                state.data.forEach { country ->
                    DropdownMenuItem(
                        text = { Text(country.name) },
                        onClick = {
                            viewModel.onCountrySelected(country)
                            expanded = false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = regionExpanded,
            onExpandedChange = {
                // Only allow expanding if a country is selected and has regions
                if (formState.selectedCountry != null) {
                    regionExpanded = !regionExpanded
                }
            }
        ) {
            OutlinedTextField(
                isError = formState.regionError != null,
                value = formState.selectedRegion?.name ?: "",
                placeholder = { Text("Select region") },
                onValueChange = {},
                readOnly = true,
                enabled = formState.selectedCountry != null, // Disable if no country
                label = { Text("Region") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, formState.selectedCountry != null)
                    .fillMaxWidth(),
                supportingText = {
                    Text(
                        formState.regionError
                            ?: "You'll see surveys that target your region"
                    )
                }
            )

            if (formState.availableRegions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = regionExpanded,
                    onDismissRequest = { regionExpanded = false }
                ) {
                    formState.availableRegions.forEach { region ->
                        DropdownMenuItem(
                            text = { Text(region.name) },
                            onClick = {
                                viewModel.onRegionSelected(region)
                                regionExpanded = false
                            }
                        )
                    }
                }
            }
        }

        PasswordTextField(
            password = formState.password ?: "",
            onPasswordChange = viewModel::onPasswordChanged,
            isError = formState.passwordError != null,
            supportingText = formState.passwordError?.let { { Text(it) } }
        )

        PasswordTextField(
            label = "Confirm password",
            password = formState.confirmPassword ?: "",
            onPasswordChange = viewModel::onConfirmPasswordChanged,
            isError = formState.confirmPasswordError != null,
            supportingText = formState.confirmPasswordError?.let { { Text(it) } }
        )

        CustomButton(
            text = "Create account",
            onClick = viewModel::register,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            isLoading = state.isLoading
        )

        TextButton(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
            Text("Already have an account? Login", textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun RoleOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) colors.primaryContainer else colors.surfaceContainerLow,
        contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = if (selected) colors.primary else colors.onSurfaceVariant)
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}
