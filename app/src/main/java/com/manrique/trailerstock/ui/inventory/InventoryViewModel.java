package com.manrique.trailerstock.ui.inventory;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.manrique.trailerstock.database.AppDatabase;
import com.manrique.trailerstock.model.Producto;
import com.manrique.trailerstock.dao.ProductoDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InventoryViewModel extends AndroidViewModel {

    private final ProductoDao productoDao;
    private final LiveData<List<Producto>> allProductos;
    private final ExecutorService executorService;

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getDatabase(application);
        productoDao = database.productoDao();
        allProductos = productoDao.getAllProductos();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Producto>> getAllProductos() {
        return allProductos;
    }

    public void insert(Producto producto) {
        executorService.execute(() -> productoDao.insertar(producto));
    }

    public void update(Producto producto) {
        executorService.execute(() -> productoDao.actualizar(producto));
    }

    public void delete(Producto producto) {
        executorService.execute(() -> productoDao.eliminar(producto));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
