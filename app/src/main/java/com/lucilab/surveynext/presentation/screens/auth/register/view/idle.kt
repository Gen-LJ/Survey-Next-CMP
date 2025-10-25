package com.lucilab.surveynext.presentation.screens.auth.register.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lucilab.surveynext.data.model.CountryModel
import com.lucilab.surveynext.presentation.screens.auth.register.RegisterFormState

@Composable
fun RegisterIdleView(
    formState: RegisterFormState,
    countries: List<CountryModel>,
    isLoading: Boolean,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Register", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = formState.name ?: "",
            onValueChange = onNameChanged,
            label = { Text("Name") },
            isError = formState.nameError != null,
            modifier = Modifier.fillMaxWidth()
        )
        formState.nameError?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        OutlinedTextField(
            value = formState.email ?: "",
            onValueChange = onEmailChanged,
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = formState.password ?: "",
            onValueChange = onPasswordChanged,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = formState.confirmPassword ?: "",
            onValueChange = onConfirmPasswordChanged,
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { /* Handle Register */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            else Text("Register")
        }


        TextButton(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val annotatedText = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    append("Already have an account? ")
                }

                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("Login")
                }
            }

            Text(
                text = annotatedText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

    }
}
