package com.example.fitness.myapplication.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fitness.myapplication.R
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaColors
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTheme
import com.example.fitness.myapplication.core.designsystem.theme.EdupiaTypography

/**
 * Ô nhập liệu chuẩn Edupia, dựng trên Material3 [OutlinedTextField].
 *
 * - [label] nằm PHÍA TRÊN ô (giống `TextFieldLabelPosition.Above` của M3), không nổi trong viền.
 * - Viền, màu, icon, prefix/suffix, supportingText, trạng thái lỗi/khoá: dùng nguyên của M3,
 *   phối màu Edupia qua [EdupiaTextFieldDefaults.colors].
 * - Khi focus (và không lỗi): vòng sáng quanh ô ([EdupiaTextFieldDefaults.FocusGlowSpread]),
 *   giống `BoxShadow(spreadRadius: 4)` của Flutter AppTextField.
 * - [supportingText] được vẽ ngay dưới ô (ngoài OutlinedTextField) để vòng sáng chỉ bao đúng ô.
 * - Bố cục trong ô: [leadingIcon] [prefix] văn bản [suffix] [trailingIcon].
 *
 * Ô mật khẩu: truyền [visualTransformation] = [PasswordVisualTransformation] và nút ẩn/hiện qua [trailingIcon].
 */
@Composable
fun EdupiaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = EdupiaTypography.bodyLarge,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = EdupiaTextFieldDefaults.shape,
    colors: TextFieldColors = EdupiaTextFieldDefaults.colors(),
    focusGlowColor: Color = EdupiaTextFieldDefaults.FocusGlowColor,
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val glowAlpha by animateFloatAsState(
        targetValue = if (focused && enabled && !isError) 1f else 0f,
        label = "EdupiaTextFieldGlow",
    )

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (label != null) {
            Text(
                text = label,
                style = EdupiaTypography.titleSmall,
                color = when {
                    !enabled -> colors.disabledLabelColor
                    isError -> colors.errorLabelColor
                    else -> colors.unfocusedLabelColor
                },
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .dropShadow(
                    shape = shape,
                    shadow = Shadow(
                        radius = 0.dp,
                        spread = EdupiaTextFieldDefaults.FocusGlowSpread,
                        color = focusGlowColor,
                        alpha = glowAlpha,
                    ),
                ),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            interactionSource = interactionSource,
            shape = shape,
            colors = colors,
        )

        // Dòng mô tả / lỗi: căn lề như supportingText của M3 (start 16dp)
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = EdupiaTypography.bodySmall,
                color = when {
                    !enabled -> colors.disabledSupportingTextColor
                    isError -> colors.errorSupportingTextColor
                    else -> colors.unfocusedSupportingTextColor
                },
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}

/** Giá trị mặc định của [EdupiaTextField]. */
object EdupiaTextFieldDefaults {
    val shape: Shape = RoundedCornerShape(8.dp)

    /** Độ loang của vòng sáng khi focus (spreadRadius của Flutter). */
    val FocusGlowSpread: Dp = 4.dp

    /** Màu vòng sáng khi focus (ShadowTextField alpha 0.7 như Flutter). */
    val FocusGlowColor: Color = EdupiaColors.ShadowTextField.copy(alpha = 0.7f)

    /**
     * Bảng màu M3 Outlined phối theo Edupia.
     * Muốn đổi vài màu: `EdupiaTextFieldDefaults.colors().copy(focusedIndicatorColor = ...)`.
     */
    @Composable
    fun colors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
        // Chữ
        focusedTextColor = EdupiaColors.Neutral900,
        unfocusedTextColor = EdupiaColors.Neutral900,
        disabledTextColor = EdupiaColors.Neutral400,
        errorTextColor = EdupiaColors.Neutral900,
        // Nền
        focusedContainerColor = EdupiaColors.White,
        unfocusedContainerColor = EdupiaColors.White,
        disabledContainerColor = EdupiaColors.Neutral100,
        errorContainerColor = EdupiaColors.White,
        // Con trỏ
        cursorColor = EdupiaColors.Primary500,
        errorCursorColor = EdupiaColors.SemanticError,
        // Viền
        focusedBorderColor = EdupiaColors.Primary300,
        unfocusedBorderColor = EdupiaColors.Neutral200,
        disabledBorderColor = EdupiaColors.Neutral200,
        errorBorderColor = EdupiaColors.SemanticError,
        // Icon
        focusedLeadingIconColor = EdupiaColors.Neutral600,
        unfocusedLeadingIconColor = EdupiaColors.Neutral600,
        disabledLeadingIconColor = EdupiaColors.Neutral400,
        errorLeadingIconColor = EdupiaColors.Neutral600,
        focusedTrailingIconColor = EdupiaColors.Neutral600,
        unfocusedTrailingIconColor = EdupiaColors.Neutral600,
        disabledTrailingIconColor = EdupiaColors.Neutral400,
        errorTrailingIconColor = EdupiaColors.SemanticError,
        // Label (label nằm ngoài ô, xem EdupiaTextField)
        focusedLabelColor = EdupiaColors.Neutral900,
        unfocusedLabelColor = EdupiaColors.Neutral900,
        disabledLabelColor = EdupiaColors.Neutral400,
        errorLabelColor = EdupiaColors.SemanticError,
        // Placeholder
        focusedPlaceholderColor = EdupiaColors.Neutral500,
        unfocusedPlaceholderColor = EdupiaColors.Neutral500,
        disabledPlaceholderColor = EdupiaColors.Neutral400,
        errorPlaceholderColor = EdupiaColors.Neutral500,
        // Dòng mô tả / lỗi phía dưới
        focusedSupportingTextColor = EdupiaColors.Neutral600,
        unfocusedSupportingTextColor = EdupiaColors.Neutral600,
        disabledSupportingTextColor = EdupiaColors.Neutral400,
        errorSupportingTextColor = EdupiaColors.SemanticError,
        // Prefix / Suffix
        focusedPrefixColor = EdupiaColors.Neutral600,
        unfocusedPrefixColor = EdupiaColors.Neutral600,
        disabledPrefixColor = EdupiaColors.Neutral400,
        errorPrefixColor = EdupiaColors.Neutral600,
        focusedSuffixColor = EdupiaColors.Neutral600,
        unfocusedSuffixColor = EdupiaColors.Neutral600,
        disabledSuffixColor = EdupiaColors.Neutral400,
        errorSuffixColor = EdupiaColors.Neutral600,
    )
}

// region Preview

/** Khung chung cho các preview: nền trắng, cách lề 16dp, các ô cách nhau 16dp. */
@Composable
private fun PreviewContainer(content: @Composable ColumnScope.() -> Unit) {
    EdupiaTheme {
        Column(
            modifier = Modifier
                .background(EdupiaColors.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

@Preview(name = "1. Mặc định", group = "EdupiaTextField", widthDp = 360)
@Composable
private fun EdupiaTextFieldDefaultPreview() = PreviewContainer {
    var name by remember { mutableStateOf("") }
    EdupiaTextField(value = name, onValueChange = { name = it }, label = "Họ và tên", placeholder = "Nhập họ và tên")
    EdupiaTextField(value = "Nguyễn Văn A", onValueChange = {}, label = "Họ và tên")
    EdupiaTextField(
        value = "Nguyễn Văn A",
        onValueChange = {},
        label = "Tên hiển thị",
        supportingText = "Tên này hiển thị trên hồ sơ của bạn",
    )
    EdupiaTextField(value = "", onValueChange = {}, placeholder = "Không có label")
}

@Preview(name = "2. Leading / Trailing icon", group = "EdupiaTextField", widthDp = 360)
@Composable
private fun EdupiaTextFieldIconPreview() = PreviewContainer {
    var keyword by remember { mutableStateOf("Tiếng Anh lớp 3") }
    EdupiaTextField(
        value = keyword,
        onValueChange = { keyword = it },
        label = "Tìm kiếm",
        placeholder = "Nhập từ khoá",
        leadingIcon = { Text("🔍") },
        trailingIcon = {
            if (keyword.isNotEmpty()) {
                IconButton(onClick = { keyword = "" }) {
                    Icon(painterResource(R.drawable.clear_text), contentDescription = "Xoá")
                }
            }
        },
    )
    EdupiaTextField(
        value = "",
        onValueChange = {},
        label = "Email",
        placeholder = "email@vidu.com",
        leadingIcon = { Text("✉️") },
    )
}

@Preview(name = "3. Prefix / Suffix", group = "EdupiaTextField", widthDp = 360)
@Composable
private fun EdupiaTextFieldPrefixSuffixPreview() = PreviewContainer {
    var phone by remember { mutableStateOf("") }
    EdupiaTextField(
        value = phone,
        onValueChange = { phone = it },
        label = "Số điện thoại",
        placeholder = "9xxxxxxxx",
        prefix = { Text("+84") },
    )
    EdupiaTextField(
        value = "500.000",
        onValueChange = {},
        label = "Học phí",
        suffix = { Text("VNĐ") },
    )
    EdupiaTextField(
        value = "nguyenvana",
        onValueChange = {},
        label = "Tài khoản",
        suffix = { Text("@edupia.vn") },
    )
    EdupiaTextField(
        value = "500.000",
        onValueChange = {},
        label = "Icon + prefix + suffix",
        leadingIcon = { Text("💰") },
        prefix = { Text("≈") },
        suffix = { Text("VNĐ") },
    )
}

@Preview(name = "4. Mật khẩu", group = "EdupiaTextField", widthDp = 360)
@Composable
private fun EdupiaTextFieldPasswordPreview() = PreviewContainer {
    var password by remember { mutableStateOf("matkhau123") }
    var visible by remember { mutableStateOf(false) }
    EdupiaTextField(
        value = password,
        onValueChange = { password = it },
        label = "Mật khẩu",
        placeholder = "Nhập mật khẩu",
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    painter = painterResource(if (visible) R.drawable.eye_open else R.drawable.eye_close),
                    contentDescription = if (visible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                )
            }
        },
    )
}

@Preview(name = "5. Lỗi", group = "EdupiaTextField", widthDp = 360)
@Composable
private fun EdupiaTextFieldErrorPreview() = PreviewContainer {
    EdupiaTextField(
        value = "091234",
        onValueChange = {},
        label = "Số điện thoại",
        prefix = { Text("+84") },
        isError = true,
        supportingText = "Số điện thoại không hợp lệ",
    )
    EdupiaTextField(
        value = "123",
        onValueChange = {},
        label = "Mật khẩu (lỗi, không có dòng mô tả)",
        isError = true,
        visualTransformation = PasswordVisualTransformation(),
    )
}

@Preview(name = "6. Khoá / Chỉ đọc", group = "EdupiaTextField", widthDp = 360)
@Composable
private fun EdupiaTextFieldDisabledPreview() = PreviewContainer {
    EdupiaTextField(
        value = "HS-2026-0001",
        onValueChange = {},
        label = "Mã học sinh (enabled = false)",
        leadingIcon = { Text("🔒") },
        suffix = { Text("#") },
        enabled = false,
    )
    EdupiaTextField(
        value = "nguyenvana@edupia.vn",
        onValueChange = {},
        label = "Email (readOnly = true)",
        readOnly = true,
        supportingText = "Liên hệ CSKH để đổi email",
    )
}

@Preview(name = "7. Nhiều dòng", group = "EdupiaTextField", widthDp = 360)
@Composable
private fun EdupiaTextFieldMultilinePreview() = PreviewContainer {
    var note by remember { mutableStateOf("Con cần luyện thêm phần nghe.\nCuối tuần ôn lại từ vựng Unit 3.") }
    EdupiaTextField(
        value = note,
        onValueChange = { note = it },
        label = "Ghi chú cho giáo viên",
        placeholder = "Nhập ghi chú",
        singleLine = false,
        minLines = 3,
        maxLines = 5,
        supportingText = "${note.length}/500",
    )
}

// endregion
