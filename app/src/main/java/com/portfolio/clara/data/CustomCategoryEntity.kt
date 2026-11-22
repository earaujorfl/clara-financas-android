package com.portfolio.clara.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "custom_categories", indices = [Index(value = ["type", "name"], unique = true)])
data class CustomCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val name: String
)
