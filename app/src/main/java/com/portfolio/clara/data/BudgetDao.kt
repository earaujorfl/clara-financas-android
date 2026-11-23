
package com.portfolio.clara.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("""
        SELECT * FROM monthly_budgets
        WHERE yearMonth = :yearMonth
        ORDER BY category ASC
    """)
    fun observeByMonth(
        yearMonth: String
    ): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)
}
