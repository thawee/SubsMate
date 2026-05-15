package com.mate.subsmate.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val iconName: String, // Material Icon name reference
    val defaultColorHex: String
)
