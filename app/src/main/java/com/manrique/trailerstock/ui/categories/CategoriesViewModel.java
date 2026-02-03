package com.manrique.trailerstock.ui.categories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.manrique.trailerstock.database.AppDatabase;
import com.manrique.trailerstock.model.Categoria;
import com.manrique.trailerstock.dao.CategoriaDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoriesViewModel extends AndroidViewModel {

    private final CategoriaDao categoriaDao;
    private final LiveData<List<Categoria>> allCategorias;
    private final ExecutorService executorService;

    public CategoriesViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getDatabase(application);
        categoriaDao = database.categoriaDao();
        allCategorias = categoriaDao.getAllCategorias();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Categoria>> getAllCategorias() {
        return allCategorias;
    }

    public void insert(Categoria categoria) {
        executorService.execute(() -> categoriaDao.insertar(categoria));
    }

    public void update(Categoria categoria) {
        executorService.execute(() -> categoriaDao.actualizar(categoria));
    }

    public void delete(Categoria categoria) {
        executorService.execute(() -> categoriaDao.eliminar(categoria));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
