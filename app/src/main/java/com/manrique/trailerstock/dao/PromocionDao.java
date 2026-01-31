package com.manrique.trailerstock.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.manrique.trailerstock.model.Promocion;
import java.util.List;

@Dao
public interface PromocionDao {

    @Insert
    long insertar(Promocion promocion);

    @Update
    void actualizar(Promocion promocion);

    @Delete
    void eliminar(Promocion promocion);

    @Query("SELECT * FROM promociones WHERE estaActiva = 1")
    List<Promocion> obtenerPromocionesActivas();

    @Query("SELECT * FROM promociones")
    List<Promocion> obtenerTodas();
}