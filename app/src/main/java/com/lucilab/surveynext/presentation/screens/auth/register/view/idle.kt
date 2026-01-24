package com.lucilab.surveynext.presentation.screens.auth.register.view
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = formState.name ?: "",
            onValueChange = viewModel::onNameChanged,
            label = { Text("Name") },
            isError = formState.nameError != null,
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                formState.nameError?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )


        OutlinedTextField(
            value = formState.email ?: "",
            onValueChange = viewModel::onEmailChanged,
            label = { Text("Email") },
            isError = formState.emailError != null,
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                formState.emailError?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )


        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                isError = formState.countryError != null,
                value = formState.selectedCountry?.name ?: "Select Country",
                onValueChange = {},
                readOnly = true,
                label = { Text("Country") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                    .fillMaxWidth(),
                supportingText = {
                    formState.countryError?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
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
                value = formState.selectedRegion?.name ?: "Select Region",
                onValueChange = {},
                readOnly = true,
                enabled = formState.selectedCountry != null, // Disable if no country
                label = { Text("Region") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                    .fillMaxWidth(),
                supportingText = {
                    formState.regionError?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
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


        OutlinedTextField(
            isError = formState.passwordError != null,
            value = formState.password ?: "",
            onValueChange = viewModel::onPasswordChanged,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                formState.passwordError?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

            }
        )

        OutlinedTextField(
            isError = formState.confirmPasswordError != null,
            value = formState.confirmPassword ?: "",
            onValueChange = viewModel::onConfirmPasswordChanged,
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                formState.confirmPasswordError?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )


        Button(
            onClick = viewModel::register,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) CircularProgressIndicator()
            else Text("Register")
        }

        TextButton(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
            Text("Already have an account? Login", textAlign = TextAlign.Center)
        }
    }
}
