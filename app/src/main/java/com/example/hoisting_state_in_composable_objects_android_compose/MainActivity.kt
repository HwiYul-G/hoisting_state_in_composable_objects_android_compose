package com.example.hoisting_state_in_composable_objects_android_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.hoisting_state_in_composable_objects_android_compose.ui.theme.Hoisting_state_in_composable_objects_android_composeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

        }
    }
}

@Preview
@Composable
fun EmailInputField(
    text : String,
    onValueChange : (String) -> Unit,
    isValid : Boolean,
    modifier : Modifier = Modifier,
    label : @Composable (()->Unit)? = null
){
    Column(modifier = modifier) {
        OutlinedTextField(
            value = text,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = label,
            isError = !isValid
        )
        if(!isValid){
            Text(
                text = "Invalid email",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.error
            )
        }
    }
}
