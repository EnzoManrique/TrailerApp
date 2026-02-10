package com.manrique.trailerstock.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity que representa un producto en el inventario del taller.
 * 
 * Migrado de Java a Kotlin data class para mejor concisión y null-safety.
 */
@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "nombre")
    val nombre: String,
    
    @ColumnInfo(name = "descripcion")
    val descripcion: String? = null,
    
    @ColumnInfo(name = "precio_costo")
    val precioCosto: Double,
    
    @ColumnInfo(name = "precio_lista")
    val precioLista: Double,
    
    @ColumnInfo(name = "precio_mayorista")
    val precioMayorista: Double,
    
    @ColumnInfo(name = "stock_actual")
    val stockActual: Int,
    
    @ColumnInfo(name = "stock_minimo")
    val stockMinimo: Int,
    
    @ColumnInfo(name = "categoria_id")
    val categoriaId: Int,  // FK a la tabla Categorias
    
    @ColumnInfo(name = "eliminado")
    val eliminado: Boolean = false  // Soft delete
) {
    /**
     * Verifica si el producto está en stock bajo
     */
    fun isLowStock(): Boolean = stockActual <= stockMinimo
    
    /**
     * Calcula el margen de ganancia con precio de lista
     */
    fun calcularMargenGanancia(): Double = precioLista - precioCosto
    
    /**
     * Calcula el porcentaje de margen
     */
    fun calcularPorcentajeMargen(): Double {
        if (precioCosto == 0.0) return 0.0
        return ((precioLista - precioCosto) / precioCosto) * 100
    }
}
