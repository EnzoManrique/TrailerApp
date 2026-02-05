package com.manrique.trailerstock;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import com.manrique.trailerstock.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private DrawerLayout drawerLayout;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar Toolbar
        setSupportActionBar(binding.toolbar);

        // Configurar DrawerLayout
        drawerLayout = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener(this);

        // Configurar Navigation Component
        // Obtener el NavHostFragment primero
        androidx.navigation.fragment.NavHostFragment navHostFragment = (androidx.navigation.fragment.NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // No configuramos setupActionBarWithNavController porque ya tenemos nuestro
        // botón personalizado
        // que abre el drawer desde la derecha
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Manejar navegación según el ítem seleccionado
        int itemId = item.getItemId();

        if (itemId == R.id.nav_statistics) {
            navController.navigate(R.id.nav_statistics);
        } else if (itemId == R.id.nav_inventory) {
            navController.navigate(R.id.nav_inventory);
        } else if (itemId == R.id.nav_sales) {
            navController.navigate(R.id.nav_sales);
        } else if (itemId == R.id.nav_categories) {
            navController.navigate(R.id.nav_categories);
        } else if (itemId == R.id.nav_promotions) {
            navController.navigate(R.id.nav_promotions);
        } else if (itemId == R.id.nav_backup) {
            navController.navigate(R.id.nav_backup);
        } else if (itemId == R.id.nav_settings) {
            navController.navigate(R.id.nav_settings);
        }

        // Cerrar el drawer después de seleccionar (lado derecho = END)
        drawerLayout.closeDrawer(GravityCompat.END);
        return true;
    }

    @Override
    public void onBackPressed() {
        // Si el drawer está abierto, cerrarlo primero
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar el menú del toolbar (botón hamburguesa a la derecha)
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Manejar clic en el botón del menú
        if (item.getItemId() == R.id.action_menu) {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                drawerLayout.openDrawer(GravityCompat.END);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
