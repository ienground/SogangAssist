package net.ienlab.sogangassist.data

import androidx.activity.result.ActivityResult
import androidx.compose.ui.graphics.vector.ImageVector

data class Permissions(
    val icon: ImageVector,
    val title: String,
    val content: String,
    val permissions: List<String>,
    val launcher: () -> Unit = {}
)
