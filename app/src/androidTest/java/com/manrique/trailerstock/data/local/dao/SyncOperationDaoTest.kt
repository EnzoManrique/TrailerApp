package com.manrique.trailerstock.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.manrique.trailerstock.data.local.AppDatabase
import com.manrique.trailerstock.data.local.entities.SyncOperation
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests unitarios para SyncOperationDao
 */
@RunWith(AndroidJUnit4::class)
class SyncOperationDaoTest {
    
    private lateinit var database: AppDatabase
    private lateinit var dao: SyncOperationDao
    
    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.syncOperationDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun testInsertAndRetrieveSyncOperation() = runBlocking {
        val operation = SyncOperation(
            entityType = "PRODUCTO",
            entityId = 1,
            operationType = "UPDATE",
            changes = """{"stockActual": 10}""",
            deviceId = "device-123"
        )
        
        val id = dao.insert(operation)
        assertTrue(id > 0)
        
        val retrieved = dao.getById(id.toInt())
        assertNotNull(retrieved)
        assertEquals("PRODUCTO", retrieved?.entityType)
        assertEquals(1, retrieved?.entityId)
    }
    
    @Test
    fun testGetPendingOperations() = runBlocking {
        // Insertar 3 operaciones pendientes
        repeat(3) { i ->
            dao.insert(SyncOperation(
                entityType = "PRODUCTO",
                entityId = i,
                operationType = "CREATE",
                changes = """{}""",
                deviceId = "device-123"
            ))
        }
        
        val pending = dao.getPendingOperations()
        assertEquals(3, pending.size)
        assertTrue(pending.all { !it.synced })
    }
    
    @Test
    fun testMarkAsSynced() = runBlocking {
        val operation = SyncOperation(
            entityType = "VENTA",
            entityId = 1,
            operationType = "CREATE",
            changes = """{}""",
            deviceId = "device-456"
        )
        
        val id = dao.insert(operation).toInt()
        dao.markAsSynced(id, System.currentTimeMillis())
        
        val updated = dao.getById(id)
        assertNotNull(updated)
        assertTrue(updated?.synced ?: false)
        assertNotNull(updated?.syncedAt)
    }
    
    @Test
    fun testIncrementRetryCount() = runBlocking {
        val operation = SyncOperation(
            entityType = "PRODUCTO",
            entityId = 1,
            operationType = "UPDATE",
            changes = """{}""",
            deviceId = "device-789"
        )
        
        val id = dao.insert(operation).toInt()
        dao.incrementRetryCount(id, "Network timeout")
        
        val updated = dao.getById(id)
        assertNotNull(updated)
        assertEquals(1, updated?.retryCount)
        assertEquals("Network timeout", updated?.failureReason)
    }
    
    @Test
    fun testCountPending() = runBlocking {
        repeat(5) { i ->
            dao.insert(SyncOperation(
                entityType = "PRODUCTO",
                entityId = i,
                operationType = "CREATE",
                changes = """{}""",
                deviceId = "device-abc"
            ))
        }
        
        val count = dao.countPending()
        assertEquals(5, count)
    }
    
    @Test
    fun testDeleteOldSyncedOperations() = runBlocking {
        val now = System.currentTimeMillis()
        val oldTime = now - (8 * 24 * 60 * 60 * 1000) // 8 días atrás
        
        // Insertar operación vieja sincronizada
        dao.insert(SyncOperation(
            entityType = "PRODUCTO",
            entityId = 1,
            operationType = "CREATE",
            changes = """{}""",
            deviceId = "device-xyz",
            synced = true,
            syncedAt = oldTime
        ))
        
        // Insertar operación reciente sincronizada
        dao.insert(SyncOperation(
            entityType = "PRODUCTO",
            entityId = 2,
            operationType = "CREATE",
            changes = """{}""",
            deviceId = "device-xyz",
            synced = true,
            syncedAt = now
        ))
        
        // Borrar operaciones más viejas de 7 días
        val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000)
        dao.deleteOldSyncedOperations(sevenDaysAgo)
        
        val remaining = dao.getPendingOperations()
        // La reciente debería seguir, la vieja debería estar borrada
        assertEquals(0, remaining.size)
    }
}
