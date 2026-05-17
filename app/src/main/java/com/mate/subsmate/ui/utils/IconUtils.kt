package com.mate.subsmate.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconUtils {
    fun getIconByName(name: String): ImageVector {
        return when (name.lowercase()) {
            "play_circle" -> Icons.Default.PlayCircle
            "music_note" -> Icons.Default.MusicNote
            "psychology" -> Icons.Default.Psychology
            "image" -> Icons.Default.Image
            "cloud" -> Icons.Default.Cloud
            "edit" -> Icons.Default.Edit
            "description" -> Icons.Default.Description
            "language" -> Icons.Default.Language
            "bolt" -> Icons.Default.Bolt
            "smartphone" -> Icons.Default.Smartphone
            "category" -> Icons.Default.Category
            "call" -> Icons.Default.Call
            "fitness_center" -> Icons.Default.FitnessCenter
            "shopping_bag" -> Icons.Default.ShoppingBag
            "school" -> Icons.Default.School
            "payments" -> Icons.Default.Payments
            else -> Icons.Default.Category
        }
    }
}
