package com.manrique.trailerstock.data.backup

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.manrique.trailerstock.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupManager(private val context: Context) {

    private val dbName = "trailer_stock_db"

    /**
     * Prepara un archivo de backup en el caché y devuelve su URI para compartir.
     */
    suspend fun getBackupUriForSharing(database: AppDatabase): Result<Pair<Uri, String>> = withContext(Dispatchers.IO) {
        try {
            // 1. Limpiar backups anteriores del caché
            context.cacheDir.listFiles { _, name -> name.startsWith("trailer_stock_backup_") }?.forEach { it.delete() }

            val db = database.openHelper.writableDatabase
            
            // 2. Forzar a SQLite a escribir TODO al archivo principal y vaciar el WAL (TRUNCATE)
            db.query("PRAGMA wal_checkpoint(TRUNCATE)").use { cursor ->
                cursor.moveToFirst()
            }

            // 3. Obtener la ruta real de la base de datos abierta
            val actualDbPath = db.path
            val dbFile = File(actualDbPath)
            
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("Base de datos no encontrada en $actualDbPath"))
            }

            // Crear archivo temporal en el caché para compartir
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val exportFile = File(context.cacheDir, "trailer_stock_backup_$timeStamp.db")
            
            FileInputStream(dbFile).use { input ->
                FileOutputStream(exportFile).use { output ->
                    input.copyTo(output)
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                exportFile
            )
            
            val info = "Tamaño: ${exportFile.length() / 1024} KB | ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}"
            Result.success(Pair(uri, info))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Exporta la base de datos actual a la ubicación seleccionada por el usuario.
     */
    suspend fun exportDatabase(destinationUri: Uri, database: AppDatabase): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Forzar a SQLite a escribir cambios del WAL al archivo principal .db de forma robusta
            val cursor = database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)")
            cursor.moveToFirst()
            cursor.close()

            val dbFile = context.getDatabasePath(dbName)
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("Base de datos no encontrada"))
            }

            context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                FileInputStream(dbFile).use { input ->
                    input.copyTo(output)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Importa una base de datos desde un archivo seleccionado por el usuario.
     * ADVERTENCIA: Esto reemplaza los datos actuales.
     */
    suspend fun importDatabase(sourceUri: Uri, database: AppDatabase): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(dbName)
            
            // 1. Cerrar la base de datos actual para evitar corrupción
            database.close()

            // 2. Copiar el archivo
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }

            // 3. Eliminar archivos auxiliares de SQLite (journal/shm/wal) si existen
            // para que no haya conflictos con el nuevo archivo .db
            File(dbFile.path + "-journal").delete()
            File(dbFile.path + "-shm").delete()
            File(dbFile.path + "-wal").delete()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
