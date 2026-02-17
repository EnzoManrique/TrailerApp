package com.manrique.trailerstock.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.manrique.trailerstock.data.local.entities.Promocion
import com.manrique.trailerstock.data.local.entities.PromocionMetodoPago
import com.manrique.trailerstock.data.local.entities.PromocionProducto

/**
 * Clase que representa una relación de Room para obtener una promoción
 * junto con sus métodos de pago y productos asociados en una sola consulta.
 */
data class PromocionConDetalles(
    @Embedded val promocion: Promocion,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "promocionId"
    )
    val metodosPago: List<PromocionMetodoPago>,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "promocionId"
    )
    val productos: List<PromocionProducto>
)
