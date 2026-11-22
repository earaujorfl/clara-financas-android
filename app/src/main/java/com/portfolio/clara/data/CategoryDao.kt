package com.portfolio.clara.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM custom_categories ORDER BY type, name COLLATE NOCASE")
    fun observeAll(): Flow<List<CustomCategoryEntity>>

    @Query("SELECT COUNT(*) FROM custom_categories WHERE type = :type AND name = :name COLLATE NOCASE")
    suspend fun countSameName(type: String, name: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CustomCategoryEntity): Long
}
