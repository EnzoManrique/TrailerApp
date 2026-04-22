package com.manrique.trailerstock.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.manrique.trailerstock.data.local.AppDatabase
import com.manrique.trailerstock.data.local.entities.StockMovement
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests unitarios para StockMovementDao
 */
@RunWith(AndroidJUnit4::class)
class StockMovementDaoTest {
    
    private lateinit var database: AppDatabase
    private lateinit var dao: StockMovementDao
    
    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.stockMovementDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun testInsertAndRetrieveStockMovement() = runBlocking {
        val movement = StockMovement(
            productId = 1,
            delta = -5,
            deviceId = "device-123",
            description = "Venta"
        )
        
        val id = dao.insert(movement)
        assertNotNull(id)
        
        val movements = dao.getByProductId(1)
        assertEquals(1, movements.size)
        assertEquals(-5, movements[0].delta)
    }
    
    @Test
    fun testCalculateTotalDelta() = runBlocking {
        // Insertar múltiples movimientos del mismo producto
        dao.insert(StockMovement(productId = 1, delta = 10, deviceId = "dev1", description = "Restock"))
        dao.insert(StockMovement(productId = 1, delta = -3, deviceId = "dev2", description = "Venta 1"))
        dao.insert(StockMovement(productId = 1, delta = -2, deviceId = "dev2", description = "Venta 2"))
        
        // El delta total debería ser 10 - 3 - 2 = 5
        val total = dao.calculateTotalDelta(1)
        assertEquals(5, total)
    }
    
    @Test
    fun testCalculateDeltaSince() = runBlocking {
        val now = System.currentTimeMillis()
        val pastTime = now - (1000 * 60 * 60) // 1 hora atrás
        
        // Movimiento antiguo
        dao.insert(StockMovement(
            productId = 1,
            delta = 100,
            deviceId = "dev1",
            timestamp = pastTime - 1000,
            description = "Old restock"
        ))
        
        // Movimientos recientes
        dao.insert(StockMovement(productId = 1, delta = 10, deviceId = "dev2", timestamp = now - 500, description = "Recent 1"))
        dao.insert(StockMovement(productId = 1, delta = -5, deviceId = "dev3", timestamp = now - 100, description = "Recent 2"))
        
        // Delta desde hace 30 minutos debería ser 10 - 5 = 5 (el antiguo no cuenta)
        val delta = dao.calculateDeltaSince(1, now - (30 * 60 * 1000))
        assertEquals(5, delta)
    }
    
    @Test
    fun testGetPendingMovements() = runBlocking {
        // Insertar movimientos sin sincronizar
        dao.insert(StockMovement(productId = 1, delta = 10, deviceId = "dev1", synced = false))
        dao.insert(StockMovement(productId = 2, delta = -5, deviceId = "dev2", synced = false))
        
        // Insertar un movimiento sincronizado
        dao.insert(StockMovement(productId = 1, delta = 3, deviceId = "dev3", synced = true))
        
        val pending = dao.getPendingMovements()
        assertEquals(2, pending.size)
    }
    
    @Test
    fun testMarkAsSynced() = runBlocking {
        val id = dao.insert(StockMovement(
            productId = 1,
            delta = 5,
            deviceId = "dev1",
            synced = false
        ))
        
        dao.markAsSynced(id.toInt(), System.currentTimeMillis())
        
        val movements = dao.getByProductId(1)
        assertEquals(1, movements.size)
        assertEquals(true, movements[0].synced)
        assertNotNull(movements[0].syncedAt)
    }
    
    @Test
    fun testMarkMultipleAsSynced() = runBlocking {
        val id1 = dao.insert(StockMovement(productId = 1, delta = 5, deviceId = "dev1", synced = false))
        val id2 = dao.insert(StockMovement(productId = 1, delta = -3, deviceId = "dev2", synced = false))
        val id3 = dao.insert(StockMovement(productId = 2, delta = 2, deviceId = "dev3", synced = false))
        
        dao.markMultipleAsSynced(listOf(id1.toInt(), id2.toInt()), System.currentTimeMillis())
        
        val allMovements = dao.getPendingMovements()
        assertEquals(1, allMovements.size)
        assertEquals(2, allMovements[0].productId)
    }
    
    @Test
    fun testGetMovementsByDateRange() = runBlocking {
        val now = System.currentTimeMillis()
        val startTime = now - (24 * 60 * 60 * 1000) // 24h atrás
        val endTime = now
        
        // Insertar movimientos dentro del rango
        dao.insert(StockMovement(productId = 1, delta = 10, deviceId = "dev1", timestamp = now - 1000))
        dao.insert(StockMovement(productId = 1, delta = -5, deviceId = "dev2", timestamp = now - 500))
        
        // Insertar movimiento fuera del rango (más viejo)
        dao.insert(StockMovement(productId = 1, delta = 20, deviceId = "dev3", timestamp = now - (48 * 60 * 60 * 1000)))
        
        val inRange = dao.getMovementsByDateRange(1, startTime, endTime)
        assertEquals(2, inRange.size)
    }
    
    @Test
    fun testCountPending() = runBlocking {
        repeat(5) {
            dao.insert(StockMovement(productId = 1, delta = 1, deviceId = "dev$it", synced = false))
        }
        
        val count = dao.countPending()
        assertEquals(5, count)
    }
}
