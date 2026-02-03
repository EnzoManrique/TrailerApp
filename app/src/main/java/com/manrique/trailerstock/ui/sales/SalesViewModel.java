package com.manrique.trailerstock.ui.sales;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.manrique.trailerstock.dao.VentaDao;
import com.manrique.trailerstock.database.AppDatabase;
import com.manrique.trailerstock.model.Venta;
import java.util.List;

public class SalesViewModel extends AndroidViewModel {

    private final VentaDao ventaDao;
    private final LiveData<List<Venta>> allVentas;

    public SalesViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getDatabase(application);
        ventaDao = database.ventaDao();
        allVentas = ventaDao.getAllVentas();
    }

    public LiveData<List<Venta>> getAllVentas() {
        return allVentas;
    }
}
