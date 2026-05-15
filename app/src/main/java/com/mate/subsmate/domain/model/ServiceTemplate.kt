package com.mate.subsmate.domain.model

data class ServiceTemplate(
    val name: String,
    val categoryId: Int,
    val monthlyPrice: Double? = null,
    val yearlyPrice: Double? = null,
    val colorHex: String,
    val iconName: String
)

object TemplateLibrary {
    val templates = listOf(
        // Streaming
        ServiceTemplate("Netflix", 1, 15.99, null, "#E50914", "play_circle"),
        ServiceTemplate("YouTube Premium", 1, 13.99, 139.99, "#FF0000", "play_circle"),
        ServiceTemplate("Spotify", 1, 10.99, 109.99, "#1DB954", "music_note"),
        ServiceTemplate("Disney+", 1, 7.99, 79.99, "#113CCF", "play_circle"),
        ServiceTemplate("Viu Premium", 1, 4.99, 49.90, "#FFD700", "play_circle"),
        
        // AI & Tools
        ServiceTemplate("ChatGPT Plus", 2, 20.00, null, "#10A37F", "psychology"),
        ServiceTemplate("Claude Pro", 2, 20.00, null, "#D97757", "psychology"),
        ServiceTemplate("Midjourney", 2, 10.00, 96.00, "#FFFFFF", "image"),
        
        // Software
        ServiceTemplate("Google One", 5, 1.99, 19.99, "#4285F4", "cloud"),
        ServiceTemplate("Adobe Creative Cloud", 5, 54.99, 599.88, "#FF0000", "edit"),
        ServiceTemplate("Microsoft 365", 5, 6.99, 69.99, "#00A4EF", "description"),

        // Utilities
        ServiceTemplate("Internet", 3, 50.00, null, "#000000", "language"),
        ServiceTemplate("Electricity", 3, null, null, "#FFD700", "bolt"),
        ServiceTemplate("Mobile Plan", 4, 30.00, null, "#007AFF", "smartphone")
    )
}
