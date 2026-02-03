package com.manrique.trailerstock.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.manrique.trailerstock.model.PromocionProducto;
import java.util.List;

@Dao
public interface PromocionProductoDao {

    @Insert
    void insertar(PromocionProducto promocionProducto);

    @Query("SELECT * FROM promocion_productos WHERE promocionId = :promocionId")
    List<PromocionProducto> obtenerProductosPorPromocion(int promocionId);

    @Query("DELETE FROM promocion_productos WHERE promocionId = :promocionId")
    void eliminarProductosPorPromocion(int promocionId);

    @Delete
    void eliminar(PromocionProducto promocionProducto);
}