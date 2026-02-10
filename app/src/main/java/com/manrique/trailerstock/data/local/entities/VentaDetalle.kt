package com.manrique.trailerstock.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entity que representa el detalle de una venta (productos vendidos).
 * Relación muchos-a-uno con Venta y Producto.
 */
@Entity(
    tableName = "venta_detalles",
    foreignKeys = [
        ForeignKey(
            entity = Venta::class,
            parentColumns = ["id"],
            childColumns = ["venta_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["producto_id"]
        )
    ]
)
data class VentaDetalle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "venta_id", index = true)
    val ventaId: Int,
    
    @ColumnInfo(name = "producto_id", index = true)
    val productoId: Int,
    
    @ColumnInfo(name = "cantidad")
    val cantidad: Int,
    
    @ColumnInfo(name = "precio_unitario")
    val precioUnitario: Double,
    
    @ColumnInfo(name = "subtotal")
    val subtotal: Double
) {
    companion object {
        /**
         * Factory method para crear VentaDetalle con cálculo automático del subtotal
         */
        fun create(
            ventaId: Int,
            productoId: Int,
            cantidad: Int,
            precioUnitario: Double
        ): VentaDetalle {
            return VentaDetalle(
                ventaId = ventaId,
                productoId = productoId,
                cantidad = cantidad,
                precioUnitario = precioUnitario,
                subtotal = cantidad * precioUnitario
            )
        }
    }
}
