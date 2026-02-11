package com.manrique.trailerstock.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.manrique.trailerstock.data.local.dao.*
import com.manrique.trailerstock.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Producto::class,
        Categoria::class,
        Promocion::class,
        PromocionProducto::class,
        PromocionMetodoPago::class,
        Venta::class,
        VentaDetalle::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun productoDao(): ProductoDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun ventaDao(): VentaDao
    abstract fun ventaDetalleDao(): VentaDetalleDao
    abstract fun promocionDao(): PromocionDao
    abstract fun promocionProductoDao(): PromocionProductoDao
    abstract fun promocionMetodoPagoDao(): PromocionMetodoPagoDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // Migración de versión 3 a 4: agregar columna eliminado a productos
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE productos ADD COLUMN eliminado INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trailer_stock_db"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration() // Recrear DB si cambia schema
                    .addCallback(DatabaseCallback())
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
        
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateSampleData(database)
                    }
                }
            }
        }
        
        private suspend fun populateSampleData(db: AppDatabase) {
            val categoriaDao = db.categoriaDao()
            
            // Solo insertar si no hay categorías
            if (kotlin.runCatching { categoriaDao.contar() }.getOrNull() == 0) {
                val categoriasBase = listOf(
                    Categoria(nombre = "Elásticos"),
                    Categoria(nombre = "Balancines"),
                    Categoria(nombre = "Guardabarros"),
                    Categoria(nombre = "Malacates"),
                    Categoria(nombre = "Rodamientos"),
                    Categoria(nombre = "Cadenas"),
                    Categoria(nombre = "Ganchos"),
                    Categoria(nombre = "Luces")
                )
                
                categoriasBase.forEach { categoria ->
                    categoriaDao.insertar(categoria)
                }
            }
        }
    }
}
