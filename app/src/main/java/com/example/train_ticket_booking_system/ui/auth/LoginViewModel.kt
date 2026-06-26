package com.example.train_ticket_booking_system.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.train_ticket_booking_system.TTBSApplication
import com.example.train_ticket_booking_system.data.entity.User
import com.example.train_ticket_booking_system.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isRegisterMode: Boolean = false,
    val isLoggedIn: Boolean = false,
    val loggedInUser: User? = null,
    val error: String? = null,
    val loading: Boolean = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as TTBSApplication).database
    private val userRepo = UserRepository(db.userDao())

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun onPhoneChange(phone: String) {
        _state.value = _state.value.copy(phone = phone.take(11), error = null)
    }

    fun onPasswordChange(pw: String) {
        _state.value = _state.value.copy(password = pw, error = null)
    }

    fun onConfirmPasswordChange(pw: String) {
        _state.value = _state.value.copy(confirmPassword = pw, error = null)
    }

    fun toggleMode() {
        _state.value = _state.value.copy(
            isRegisterMode = !_state.value.isRegisterMode,
            error = null,
            password = "",
            confirmPassword = ""
        )
    }

    fun submit() {
        val s = _state.value
        if (s.phone.length != 11) { _state.value = s.copy(error = "请输入11位手机号"); return }
        if (s.password.length < 6) { _state.value = s.copy(error = "密码至少6位"); return }

        if (s.isRegisterMode) {
            if (s.password != s.confirmPassword) { _state.value = s.copy(error = "两次密码不一致"); return }
            register(s.phone, s.password)
        } else {
            login(s.phone, s.password)
        }
    }

    private fun login(phone: String, password: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val user = userRepo.login(phone, password)
                if (user != null) {
                    _state.value = _state.value.copy(loading = false, isLoggedIn = true, loggedInUser = user)
                } else {
                    _state.value = _state.value.copy(loading = false, error = "手机号或密码错误")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "登录失败，请重试")
            }
        }
    }

    private fun register(phone: String, password: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val user = userRepo.register(phone, password)
                if (user != null) {
                    _state.value = _state.value.copy(loading = false, isLoggedIn = true, loggedInUser = user)
                } else {
                    _state.value = _state.value.copy(loading = false, error = "该手机号已注册")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "注册失败，请重试")
            }
        }
    }

    fun devLogin() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val devPhone = "13800000000"
                val devPwd = "123456"
                var user = userRepo.login(devPhone, devPwd)
                if (user == null) {
                    user = userRepo.register(devPhone, devPwd)
                    userRepo.setPaymentPassword(devPhone, "123456")
                }
                _state.value = _state.value.copy(loading = false, isLoggedIn = true, loggedInUser = user, phone = devPhone)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "开发者登录失败")
            }
        }
    }
}
