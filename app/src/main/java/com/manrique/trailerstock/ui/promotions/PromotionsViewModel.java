package com.manrique.trailerstock.ui.promotions;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.manrique.trailerstock.database.AppDatabase;
import com.manrique.trailerstock.model.Promocion;

import java.util.List;

public class PromotionsViewModel extends AndroidViewModel {

    private final AppDatabase database;
    private final MutableLiveData<List<Promocion>> promocionesLiveData;

    public PromotionsViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getDatabase(application);
        promocionesLiveData = new MutableLiveData<>();
        loadPromociones();
    }

    public LiveData<List<Promocion>> getAllPromociones() {
        return promocionesLiveData;
    }

    public void loadPromociones() {
        new Thread(() -> {
            List<Promocion> promociones = database.promocionDao().obtenerTodas();
            promocionesLiveData.postValue(promociones);
        }).start();
    }

    public void insert(Promocion promocion) {
        new Thread(() -> {
            database.promocionDao().insertar(promocion);
            loadPromociones();
        }).start();
    }

    public void update(Promocion promocion) {
        new Thread(() -> {
            database.promocionDao().actualizar(promocion);
            loadPromociones();
        }).start();
    }

    public void delete(Promocion promocion) {
        new Thread(() -> {
            database.promocionDao().eliminar(promocion);
            loadPromociones();
        }).start();
    }

    public void toggleActive(Promocion promocion, boolean isActive) {
        new Thread(() -> {
            promocion.setEstaActiva(isActive);
            database.promocionDao().actualizar(promocion);
            loadPromociones();
        }).start();
    }
}
