package com.manrique.trailerstock.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.manrique.trailerstock.model.Categoria;
import java.util.List;

@Dao
public interface CategoriaDao {

    @Insert
    void insertar(Categoria categoria);

    @Update
    void actualizar(Categoria categoria);

    @Delete
    void eliminar(Categoria categoria);

    @Query("SELECT * FROM categorias ORDER BY nombre ASC")
    LiveData<List<Categoria>> getAllCategorias();

    @Query("SELECT * FROM categorias ORDER BY nombre ASC")
    List<Categoria> obtenerTodas();

    @Query("SELECT * FROM categorias WHERE id = :id")
    Categoria obtenerPorId(int id);
}