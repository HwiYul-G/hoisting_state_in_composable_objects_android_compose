package com.example.hoisting_state_in_composable_objects_android_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hoisting_state_in_composable_objects_android_compose.ui.theme.Hoisting_state_in_composable_objects_android_composeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Hoisting_state_in_composable_objects_android_composeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 16.dp)
                    ) {

                        EmailInputField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            label = { Text(text = "Email address") },
                        )

                        val state = rememberEmailInputFieldState()
                        EmailInputField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            state = state,
                            label = { Text(text = "Email address") },
                        )
                        Text(
                            text = "Email [${state.email}] is  ${(if (state.isValid) "valid" else "invalid")}",
                            modifier = Modifier.padding(top = 16.dp),
                            style = MaterialTheme.typography.body1,
                        )
                    }

                }
            }
        }

    }

    @Composable
    fun rememberEmailInputFieldState(): EmailInputFieldState = rememberSaveable(
        saver = EmailInputFieldStateImpl.Saver
    ) {
        EmailInputFieldStateImpl("")
    }

    @Composable
    fun EmailInputField(
        modifier: Modifier = Modifier,
        state: EmailInputFieldState = rememberEmailInputFieldState(),
        label: @Composable (() -> Unit)? = null,
    ) {
        Column(
            modifier = modifier
        ) {
            OutlinedTextField(
                value = state.email,
                onValueChange = { value -> state.email = value },
                modifier = Modifier.fillMaxWidth(),
                label = label,
                isError = !state.isValid,
            )
            if (!state.isValid) {
                Text(
                    text = "Invalid email",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.error,
                )
            }
        }
    }

    @Preview()
    @Composable
    fun EmailInputField2Preview() {
        Hoisting_state_in_composable_objects_android_composeTheme {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp)
            ) {
                EmailInputField(
                    label = { Text(text = "Email address") },
                )
            }
        }
    }
}
