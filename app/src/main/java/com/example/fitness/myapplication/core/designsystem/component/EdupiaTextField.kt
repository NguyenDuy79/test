package com.example.fitness.myapplication.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaColors
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaShapes
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTheme

/**
 * Ô nhập liệu chuẩn (TextField) cho toàn bộ ứng dụng Edupia.
 */
@Composable
fun EdupiaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        shape = EdupiaShapes.ShapeMedium,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon,
        isError = isError,
        supportingText = errorMessage?.let { { Text(it, color = EdupiaColors.SemanticError) } },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EdupiaColors.Primary,
            unfocusedBorderColor = EdupiaColors.Neutral300,
            focusedLabelColor = EdupiaColors.Primary,
        ),
    )
}

@Preview(showBackground = true, name = "EdupiaTextField - States")
@Composable
fun EdupiaTextFieldPreview() {
    EdupiaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EdupiaTextField(
                value = "",
                onValueChange = {},
                label = "Số điện thoại",
                placeholder = "09xxxxxxxx"
            )
            EdupiaTextField(
                value = "0912345678",
                onValueChange = {},
                label = "Họ và tên"
            )
            EdupiaTextField(
                value = "123",
                onValueChange = {},
                label = "Mật khẩu",
                isError = true,
                errorMessage = "Mật khẩu tối thiểu 6 ký tự"
            )
        }
    }
}
