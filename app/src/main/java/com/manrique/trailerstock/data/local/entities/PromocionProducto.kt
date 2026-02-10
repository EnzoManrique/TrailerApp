package com.manrique.trailerstock.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entity de relación muchos-a-muchos entre Promoción y Producto.
 * Una promoción puede tener varios productos, y un producto puede estar en varias promociones.
 */
@Entity(
    tableName = "promocion_productos",
    primaryKeys = ["promocionId", "productoId"],
    foreignKeys = [
        ForeignKey(
            entity = Promocion::class,
            parentColumns = ["id"],
            childColumns = ["promocionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productoId"), Index("promocionId")]
)
data class PromocionProducto(
    val promocionId: Int,
    val productoId: Int,
    val cantidadRequerida: Int
)
