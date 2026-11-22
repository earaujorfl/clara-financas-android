package com.portfolio.clara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.portfolio.clara.data.FinanceDatabase
import com.portfolio.clara.data.FinanceRepository
import com.portfolio.clara.ui.ClaraApp
import com.portfolio.clara.ui.FinanceViewModel

class MainActivity : ComponentActivity() {
    private val database by lazy {
        Room.databaseBuilder(applicationContext, FinanceDatabase::class.java, "clara-finances.db")
            .addMigrations(FinanceDatabase.MIGRATION_1_2)
            .build()
    }
    private val repository by lazy { FinanceRepository(database.transactionDao(), database.categoryDao()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val financeViewModel: FinanceViewModel = viewModel(factory = FinanceViewModel.factory(repository))
            ClaraApp(financeViewModel)
        }
    }
}
