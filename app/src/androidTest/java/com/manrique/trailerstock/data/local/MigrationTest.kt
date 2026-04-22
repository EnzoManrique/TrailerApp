package com.manrique.trailerstock.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

/**
 * Test de migración de Room v10 → v11
 * Verifica que el schema se actualice correctamente sin pérdida de datos
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    
    private val TEST_DB = "migration-test"
    
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )
    
    @Test
    fun migrate10To11() {
        // Crear DB en versión 10
        val db = helper.createDatabase(TEST_DB, 10)
        
        // Insertar datos de prueba en versión 10
        db.execSQL("""
            INSERT INTO productos (nombre, precio_costo, precio_lista, precio_mayorista, stock_actual, stock_minimo, categoria_id, eliminado)
            VALUES ('Cilindro', 100.0, 150.0, 120.0, 10, 5, 1, 0)
        """)
        
        db.execSQL("""
            INSERT INTO ventas (fecha, total, tipo_cliente, metodo_pago, estado)
            VALUES (${System.currentTimeMillis()}, 500.0, 'LISTA', 'EFECTIVO', 'ACTIVA')
        """)
        
        db.close()
        
        // Ejecutar migración
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB,
            11,
            true,
            AppDatabase.MIGRATION_10_11
        )
        
        // Verificar que las nuevas columnas existen en productos
        val productoCursor = migratedDb.query("""
            SELECT synced_at, device_id, remote_id FROM productos LIMIT 1
        """)
        assertEquals(1, productoCursor.count)
        productoCursor.close()
        
        // Verificar que las nuevas columnas existen en ventas
        val ventaCursor = migratedDb.query("""
            SELECT synced_at, device_id, remote_id FROM ventas LIMIT 1
        """)
        assertEquals(1, ventaCursor.count)
        ventaCursor.close()
        
        // Verificar que nuevas tablas existen
        val syncOpCursor = migratedDb.query("""
            SELECT name FROM sqlite_master WHERE type='table' AND name='sync_operations'
        """)
        assertEquals(1, syncOpCursor.count)
        syncOpCursor.close()
        
        val stockMoveCursor = migratedDb.query("""
            SELECT name FROM sqlite_master WHERE type='table' AND name='stock_movements'
        """)
        assertEquals(1, stockMoveCursor.count)
        stockMoveCursor.close()
        
        migratedDb.close()
    }
}
