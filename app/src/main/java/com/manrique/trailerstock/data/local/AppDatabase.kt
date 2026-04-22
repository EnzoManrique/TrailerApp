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
        VentaDetalle::class,
        SyncOperation::class,
        StockMovement::class
    ],
    version = 11,
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
    abstract fun syncOperationDao(): SyncOperationDao
    abstract fun stockMovementDao(): StockMovementDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // Migración de versión 3 a 4: agregar columna eliminado a productos
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE productos ADD COLUMN eliminado INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        // Migración de versión 8 a 9: agregar columna metodo_pago a ventas
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ventas ADD COLUMN metodo_pago TEXT NOT NULL DEFAULT 'EFECTIVO'")
            }
        }
        
        // Migración de versión 9 a 10: agregar columna estado a ventas
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ventas ADD COLUMN estado TEXT NOT NULL DEFAULT 'ACTIVA'")
            }
        }
        
        // Migración de versión 10 a 11: agregar soporte para sincronización Firebase
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar columnas de sync a Producto
                database.execSQL("ALTER TABLE productos ADD COLUMN synced_at INTEGER")
                database.execSQL("ALTER TABLE productos ADD COLUMN device_id TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE productos ADD COLUMN remote_id TEXT")
                
                // Agregar columnas de sync a Venta
                database.execSQL("ALTER TABLE ventas ADD COLUMN synced_at INTEGER")
                database.execSQL("ALTER TABLE ventas ADD COLUMN device_id TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE ventas ADD COLUMN remote_id TEXT")
                
                // Agregar columnas de sync a VentaDetalle
                database.execSQL("ALTER TABLE venta_detalles ADD COLUMN synced_at INTEGER")
                
                // Crear tabla SyncOperation
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS sync_operations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        entity_type TEXT NOT NULL,
                        entity_id INTEGER NOT NULL,
                        operation_type TEXT NOT NULL,
                        changes TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        device_id TEXT NOT NULL,
                        synced INTEGER NOT NULL DEFAULT 0,
                        synced_at INTEGER,
                        retry_count INTEGER NOT NULL DEFAULT 0,
                        failure_reason TEXT
                    )
                """)
                
                // Crear índices para SyncOperation
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_sync_pending ON sync_operations(synced, timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_sync_entity ON sync_operations(entity_type, entity_id)")
                
                // Crear tabla StockMovement
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS stock_movements (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        product_id INTEGER NOT NULL,
                        delta INTEGER NOT NULL,
                        device_id TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        description TEXT,
                        synced_at INTEGER,
                        synced INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                // Crear índices para StockMovement
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_product ON stock_movements(product_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_stock_timestamp ON stock_movements(timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_stock_pending ON stock_movements(synced, timestamp)")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trailer_stock_db"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                    .fallbackToDestructiveMigration() // Recrear DB si cambia schema
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}
