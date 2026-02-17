package com.manrique.trailerstock.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.manrique.trailerstock.data.local.entities.MetodoPago

/**
 * Entidad que representa la relación muchos-a-muchos entre promociones y métodos de pago.
 * 
 * Una promoción puede tener múltiples métodos de pago permitidos.
 * Si no tiene ninguno asociado, significa que aplica a todos los métodos.
 */
@Entity(
    tableName = "promocion_metodos_pago",
    primaryKeys = ["promocionId", "metodoPago"],
    foreignKeys = [
        ForeignKey(
            entity = Promocion::class,
            parentColumns = ["id"],
            childColumns = ["promocionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PromocionMetodoPago(
    @ColumnInfo(name = "promocionId")
    val promocionId: Int,
    
    @ColumnInfo(name = "metodoPago")
    val metodoPago: MetodoPago
)
