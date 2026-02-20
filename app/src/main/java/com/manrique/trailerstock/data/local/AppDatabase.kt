package com.manrique.trailerstock.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.manrique.trailerstock.data.local.dao.*
import com.manrique.trailerstock.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
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
        
        // Migración de versión 8 a 9: agregar columna metodo_pago a ventas
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ventas ADD COLUMN metodo_pago TEXT NOT NULL DEFAULT 'EFECTIVO'")
            }
        }
        
        // Migración de versión 9 a 10: agregar columna estado a ventas
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ventas ADD COLUMN estado TEXT NOT NULL DEFAULT 'ACTIVA'")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trailer_stock_db"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_8_9, MIGRATION_9_10)
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
        
        suspend fun resetAndPopulateSampleData(db: AppDatabase) = withContext(Dispatchers.IO) {
            db.clearAllTables()
            populateSampleData(db)
        }

        private suspend fun populateSampleData(db: AppDatabase) {
            val categoriaDao = db.categoriaDao()
            val productoDao = db.productoDao()
            
            // Insertar categorías
            val categoriasBase = listOf(
                Categoria(nombre = "Elásticos"),
                Categoria(nombre = "Ejes"),
                Categoria(nombre = "Guardabarros"),
                Categoria(nombre = "Malacates"),
                Categoria(nombre = "Luces")
            )
            
            val catIds = categoriasBase.map { categoriaDao.insertar(it) }

            // Insertar algunos productos de prueba
            if (catIds.isNotEmpty()) {
                val productosPrueba = listOf(
                    Producto(
                        nombre = "Kit Elásticos 5 Hojas",
                        precioCosto = 45000.0,
                        precioLista = 65000.0,
                        precioMayorista = 55000.0,
                        stockActual = 10,
                        stockMinimo = 5,
                        categoriaId = catIds[0].toInt()
                    ),
                    Producto(
                        nombre = "Eje 1.5tn Standard",
                        precioCosto = 120000.0,
                        precioLista = 180000.0,
                        precioMayorista = 150000.0,
                        stockActual = 4,
                        stockMinimo = 2,
                        categoriaId = catIds[1].toInt()
                    ),
                    Producto(
                        nombre = "Guardabarros Plástico Simple",
                        precioCosto = 8000.0,
                        precioLista = 15000.0,
                        precioMayorista = 11000.0,
                        stockActual = 20,
                        stockMinimo = 10,
                        categoriaId = catIds[2].toInt()
                    )
                )
                productosPrueba.forEach { productoDao.insertar(it) }
            }
        }
    }
}
