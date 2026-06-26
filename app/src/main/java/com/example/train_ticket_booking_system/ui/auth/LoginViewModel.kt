package com.example.train_ticket_booking_system.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.train_ticket_booking_system.TTBSApplication
import com.example.train_ticket_booking_system.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class LoginUiState(
    val phone: String = "",
    val verificationCode: String = "",
    val generatedCode: String = "",
    val codeSent: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as TTBSApplication).database
    private val userRepo = UserRepository(db.userDao())

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun onPhoneChange(phone: String) {
        _state.value = _state.value.copy(phone = phone.take(11), error = null)
    }

    fun sendVerificationCode() {
        val phone = _state.value.phone
        if (phone.length != 11) {
            _state.value = _state.value.copy(error = "请输入11位手机号")
            return
        }
        val code = Random.nextInt(100000, 999999).toString()
        _state.value = _state.value.copy(
            generatedCode = code,
            codeSent = true,
            error = null
        )
    }

    fun onCodeChange(code: String) {
        _state.value = _state.value.copy(verificationCode = code.take(6), error = null)
    }

    fun verifyCode() {
        if (_state.value.verificationCode != _state.value.generatedCode) {
            _state.value = _state.value.copy(error = "验证码错误")
            return
        }
        viewModelScope.launch {
            val existing = userRepo.getByPhone(_state.value.phone)
            if (existing == null) {
                userRepo.register(_state.value.phone)
            }
            _state.value = _state.value.copy(isLoggedIn = true)
        }
    }

    fun resetLogin() {
        _state.value = LoginUiState()
    }
}
