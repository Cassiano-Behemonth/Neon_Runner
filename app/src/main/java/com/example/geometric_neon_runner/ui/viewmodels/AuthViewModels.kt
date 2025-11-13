package com.example.geometric_neon_runner.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geometric_neon_runner.data.model.User
import com.example.geometric_neon_runner.data.repository.AuthRepository
import com.example.geometric_neon_runner.utils.Result
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException // Importação necessária
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Result<User>?>(null)
    val loginState: StateFlow<Result<User>?> = _loginState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun login(email: String, password: String) {
        if (!validateEmail(email)) {
            _loginState.value = Result.Error("Email inválido")
            return
        }

        if (!validatePassword(password)) {
            _loginState.value = Result.Error("Senha deve ter no mínimo 6 caracteres")
            return
        }

        _isLoading.value = true
        _loginState.value = Result.Loading

        viewModelScope.launch {
            try {
                val result = authRepository.login(email, password)
                _loginState.value = result
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _loginState.value = Result.Error("Email ou senha inválidos", e)
            } catch (e: Exception) {
                _loginState.value = Result.Error(e.message ?: "Erro desconhecido ao fazer login", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validatePassword(password: String): Boolean {
        return password.length >= 6
    }

    fun clearError() {
        _loginState.value = null
    }
}


class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<Result<User>?>(null)
    val registerState: StateFlow<Result<User>?> = _registerState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun register(email: String, password: String, username: String) {
        if (!validateUsername(username)) {
            _registerState.value = Result.Error("Username deve ter pelo menos 3 caracteres")
            return
        }

        if (!validateEmail(email)) {
            _registerState.value = Result.Error("Email inválido")
            return
        }

        if (!validatePassword(password)) {
            _registerState.value = Result.Error("Senha deve ter no mínimo 6 caracteres")
            return
        }

        _isLoading.value = true
        _registerState.value = Result.Loading

        viewModelScope.launch {
            try {
                val result = authRepository.register(email, password, username)
                _registerState.value = result
            } catch (e: Exception) {
                _registerState.value = Result.Error(e.message ?: "Erro ao cadastrar", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun validateUsername(username: String): Boolean {
        return username.length >= 3
    }

    fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validatePassword(password: String): Boolean {
        return password.length >= 6
    }

    fun clearError() {
        _registerState.value = null
    }
}