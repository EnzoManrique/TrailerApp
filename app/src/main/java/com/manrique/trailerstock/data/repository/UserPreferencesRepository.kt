package com.manrique.trailerstock.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val SHOW_EARNINGS = booleanPreferencesKey("show_earnings")
        val SHOW_SALES = booleanPreferencesKey("show_sales")
        val SHOW_LOW_STOCK = booleanPreferencesKey("show_low_stock")
        val SHOW_TICKET = booleanPreferencesKey("show_ticket")
        val SHOW_CAPITAL = booleanPreferencesKey("show_capital")
        val SHOW_TOP_PRODUCTS = booleanPreferencesKey("show_top_products")
        val SHOW_STAGNANT_PRODUCTS = booleanPreferencesKey("show_stagnant_products")
        val SHOW_SALES_BY_CATEGORY = booleanPreferencesKey("show_sales_by_category")
        val SHOW_MOST_PROFITABLE = booleanPreferencesKey("show_most_profitable")
        val STAGNANT_THRESHOLD_DAYS = intPreferencesKey("stagnant_threshold_days")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                showEarnings = preferences[PreferencesKeys.SHOW_EARNINGS] ?: true,
                showSales = preferences[PreferencesKeys.SHOW_SALES] ?: true,
                showLowStock = preferences[PreferencesKeys.SHOW_LOW_STOCK] ?: true,
                showTicket = preferences[PreferencesKeys.SHOW_TICKET] ?: true,
                showCapital = preferences[PreferencesKeys.SHOW_CAPITAL] ?: true,
                showTopProducts = preferences[PreferencesKeys.SHOW_TOP_PRODUCTS] ?: true,
                showStagnantProducts = preferences[PreferencesKeys.SHOW_STAGNANT_PRODUCTS] ?: true,
                showSalesByCategory = preferences[PreferencesKeys.SHOW_SALES_BY_CATEGORY] ?: true,
                showMostProfitable = preferences[PreferencesKeys.SHOW_MOST_PROFITABLE] ?: true,
                stagnantThresholdDays = preferences[PreferencesKeys.STAGNANT_THRESHOLD_DAYS] ?: 30
            )
        }

    suspend fun updateVisibility(key: String, visible: Boolean) {
        context.dataStore.edit { preferences ->
            when (key) {
                "earnings" -> preferences[PreferencesKeys.SHOW_EARNINGS] = visible
                "sales" -> preferences[PreferencesKeys.SHOW_SALES] = visible
                "low_stock" -> preferences[PreferencesKeys.SHOW_LOW_STOCK] = visible
                "ticket" -> preferences[PreferencesKeys.SHOW_TICKET] = visible
                "capital" -> preferences[PreferencesKeys.SHOW_CAPITAL] = visible
                "top_products" -> preferences[PreferencesKeys.SHOW_TOP_PRODUCTS] = visible
                "stagnant" -> preferences[PreferencesKeys.SHOW_STAGNANT_PRODUCTS] = visible
                "category_sales" -> preferences[PreferencesKeys.SHOW_SALES_BY_CATEGORY] = visible
                "profitable" -> preferences[PreferencesKeys.SHOW_MOST_PROFITABLE] = visible
            }
        }
    }

    suspend fun updateStagnantThreshold(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.STAGNANT_THRESHOLD_DAYS] = days
        }
    }
}

data class UserPreferences(
    val showEarnings: Boolean,
    val showSales: Boolean,
    val showLowStock: Boolean,
    val showTicket: Boolean,
    val showCapital: Boolean,
    val showTopProducts: Boolean,
    val showStagnantProducts: Boolean,
    val showSalesByCategory: Boolean,
    val showMostProfitable: Boolean,
    val stagnantThresholdDays: Int
)
