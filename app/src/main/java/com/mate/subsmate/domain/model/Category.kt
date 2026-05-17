package com.mate.subsmate.domain.model

data class Category(
    val id: Int,
    val name: String,
    val iconName: String,
    val colorHex: String
)

object CategoryDefaults {
    val categories = listOf(
        Category(1, "Entertainment", "play_circle", "#E50914"),
        Category(2, "AI & Tools", "psychology", "#10A37F"),
        Category(3, "Utilities", "bolt", "#F57C00"),
        Category(4, "Communication", "call", "#007AFF"),
        Category(5, "Software", "code", "#6200EE"),
        Category(7, "Health", "fitness_center", "#4CAF50"),
        Category(8, "Lifestyle", "shopping_bag", "#FF9800"),
        Category(9, "Education", "school", "#2196F3"),
        Category(10, "Finance", "payments", "#43A047"),
        Category(99, "Other", "category", "#616161")
    )
}
