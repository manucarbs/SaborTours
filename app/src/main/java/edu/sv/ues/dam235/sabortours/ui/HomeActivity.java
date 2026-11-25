package edu.sv.ues.dam235.sabortours.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.ui.explorar.ExplorarFragment;
import edu.sv.ues.dam235.sabortours.ui.home.HomeFragment;
import edu.sv.ues.dam235.sabortours.ui.perfil.PerfilFragment;
import edu.sv.ues.dam235.sabortours.ui.reservas.ReservasFragment;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Pantalla inicial (solo la primera vez, no al rotar el teléfono)
        if (savedInstanceState == null) {
            mostrar(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment;

            if (id == R.id.nav_inicio) fragment = new HomeFragment();
            else if (id == R.id.nav_explorar) fragment = new ExplorarFragment();
            else if (id == R.id.nav_reservas) fragment = new ReservasFragment();
            else if (id == R.id.nav_perfil) fragment = new PerfilFragment();
            else return false;

            mostrar(fragment);
            return true;
        });
    }

    private void mostrar(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}