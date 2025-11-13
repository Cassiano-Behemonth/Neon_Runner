package com.example.geometric_neon_runner.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.geometric_neon_runner.ui.components.NeonButton
import com.example.geometric_neon_runner.ui.components.NeonTextField
import com.example.geometric_neon_runner.ui.components.LoadingIndicator
import com.example.geometric_neon_runner.ui.navigation.Screen
import com.example.geometric_neon_runner.ui.theme.NeonTunnelTheme
import com.example.geometric_neon_runner.ui.viewmodels.LoginViewModel
import com.example.geometric_neon_runner.ui.viewmodels.RegisterViewModel
import com.example.geometric_neon_runner.utils.Result
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var hasAttemptedLogin by remember { mutableStateOf(false) }

    val loginState by viewModel.loginState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val isEmailValid = remember(email) {
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    val isPasswordValid = remember(password) {
        password.length >= 6
    }

    val showEmailError = hasAttemptedLogin && email.isNotEmpty() && !isEmailValid
    val showPasswordError = hasAttemptedLogin && password.isNotEmpty() && !isPasswordValid

    val isLoginButtonEnabled = email.isNotEmpty() && password.isNotEmpty() && isEmailValid && isPasswordValid

    LaunchedEffect(loginState) {
        if (loginState is Result.Success) {
            navController.navigate(Screen.Menu.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    NeonTunnelTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NEON RUNNER",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    NeonTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        isError = showEmailError,
                        supportingText = if (showEmailError) {
                            { Text("Email inválido") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    NeonTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Senha",
                        isError = showPasswordError,
                        supportingText = if (showPasswordError) {
                            { Text("Senha inválida (mín. 6 caracteres)") }
                        } else null,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isLoading) {
                        LoadingIndicator(modifier = Modifier.size(48.dp))
                    } else {
                        NeonButton(
                            text = "ENTRAR",
                            onClick = {
                                hasAttemptedLogin = true
                                if (isLoginButtonEnabled) {
                                    viewModel.login(email, password)
                                }
                            },
                            enabled = isLoginButtonEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { navController.navigate(Screen.Register.route) }
                    ) {
                        Text(
                            text = "Não tem uma conta? Cadastre-se",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (loginState is Result.Error) {
                        Spacer(modifier = Modifier.height(16.dp))
                        val errorMessage = (loginState as Result.Error).message
                        val displayMessage = if (errorMessage.contains("invalid", ignoreCase = true)) {
                            "Usuário ou senha inválidos"
                        } else {
                            errorMessage
                        }
                        Text(
                            text = displayMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var hasAttemptedRegister by remember { mutableStateOf(false) }

    val registerState by viewModel.registerState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val isUsernameValid = remember(username) { username.length >= 3 }
    val isEmailValid = remember(email) { Patterns.EMAIL_ADDRESS.matcher(email).matches() }
    val isPasswordValid = remember(password) { password.length >= 6 }

    val showUsernameError = hasAttemptedRegister && username.isNotEmpty() && !isUsernameValid
    val showEmailError = hasAttemptedRegister && email.isNotEmpty() && !isEmailValid
    val showPasswordError = hasAttemptedRegister && password.isNotEmpty() && !isPasswordValid

    val isRegisterButtonEnabled = username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()
            && isUsernameValid && isEmailValid && isPasswordValid

    LaunchedEffect(registerState) {
        if (registerState is Result.Success) {
            showSuccessMessage = true
            delay(1000)
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Register.route) { inclusive = true }
            }
        }
    }

    NeonTunnelTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CRIAR CONTA",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    NeonTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Nome de usuário",
                        isError = showUsernameError,
                        supportingText = if (showUsernameError) {
                            { Text("Nome de usuário inválido (mín. 3 caracteres)") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    NeonTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        isError = showEmailError,
                        supportingText = if (showEmailError) {
                            { Text("Email inválido") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    NeonTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Senha",
                        isError = showPasswordError,
                        supportingText = if (showPasswordError) {
                            { Text("Senha inválida (mín. 6 caracteres)") }
                        } else null,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isLoading) {
                        LoadingIndicator(modifier = Modifier.size(48.dp))
                    } else {
                        NeonButton(
                            text = "CADASTRAR",
                            onClick = {
                                hasAttemptedRegister = true
                                if (isRegisterButtonEnabled) {
                                    viewModel.register(email, password, username)
                                }
                            },
                            enabled = isRegisterButtonEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        }
                    ) {
                        Text(
                            text = "Já tem uma conta? Faça Login",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (registerState is Result.Error) {
                        Spacer(modifier = Modifier.height(16.dp))
                        val errorMsg = (registerState as Result.Error).message

                        val displayMessage = when {
                            errorMsg.contains("email address is already in use", ignoreCase = true) ||
                                    errorMsg.contains("already exists", ignoreCase = true) ||
                                    errorMsg.contains("already in use", ignoreCase = true) ->
                                "Este email já está em uso. Tente outro ou faça login."
                            else -> errorMsg
                        }

                        Text(
                            text = displayMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (showSuccessMessage) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "✓ Conta criada com sucesso! Redirecionando para login...",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
