package com.manrique.trailerstock.data.repository

import com.manrique.trailerstock.data.local.dao.CategoriaDao
import com.manrique.trailerstock.data.local.entities.Categoria
import kotlinx.coroutines.flow.Flow

/**
 * Repository para operaciones de Categoría.
 */
class CategoriaRepository(private val categoriaDao: CategoriaDao) {
    
    val allCategorias: Flow<List<Categoria>> = categoriaDao.obtenerTodas()
    
    suspend fun insert(categoria: Categoria): Long {
        return categoriaDao.insertar(categoria)
    }
    
    suspend fun update(categoria: Categoria) {
        categoriaDao.actualizar(categoria)
    }
    
    suspend fun delete(categoria: Categoria) {
        categoriaDao.eliminar(categoria)
    }
    
    suspend fun softDelete(categoria: Categoria) {
        categoriaDao.softDelete(categoria.id)
    }
    
    suspend fun getById(id: Int): Categoria? {
        return categoriaDao.obtenerPorId(id)
    }
    
    suspend fun count(): Int {
        return categoriaDao.contar()
    }
}
