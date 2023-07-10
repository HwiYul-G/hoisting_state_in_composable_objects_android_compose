package com.example.hoisting_state_in_composable_objects_android_compose

import android.util.Patterns
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue

class EmailInputFieldStateImpl(
    email: String = "",
) : EmailInputFieldState {
    private var _email by mutableStateOf(email)
    override var email: String
        get() = _email
        set(value) {
            _email = value
        }

    override val isValid by derivedStateOf { isValidEmail(_email) }

    private fun isValidEmail(email: String): Boolean =
        email.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(email).matches()

    companion object {
        val Saver = Saver<EmailInputFieldStateImpl, List<Any>>(
            save = { listOf(it._email) },
            restore = {
                EmailInputFieldStateImpl(
                    email = it[0] as String,
                )
            }
        )
    }
}