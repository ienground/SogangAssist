package net.ienlab.sogangassist.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
private val defaultTypography = Typography()
val Typography =
    Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = Pretendard),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = Pretendard),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = Pretendard),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = Pretendard),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = Pretendard),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = Pretendard),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = Pretendard),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = Pretendard),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = Pretendard),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = Pretendard, fontSize = 14.sp),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = Pretendard, fontSize = 12.sp),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = Pretendard, fontSize = 11.sp),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = Pretendard),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = Pretendard),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = Pretendard),
    )