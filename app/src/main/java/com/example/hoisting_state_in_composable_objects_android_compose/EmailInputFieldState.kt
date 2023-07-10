package com.example.hoisting_state_in_composable_objects_android_compose

import androidx.compose.runtime.Stable

@Stable
interface EmailInputFieldState {
    var email : String
    val isValid : Boolean
}