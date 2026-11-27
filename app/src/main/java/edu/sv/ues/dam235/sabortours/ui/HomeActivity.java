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
import edu.sv.ues.dam235.sabortours.data.repository.RestauranteRepository;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import edu.sv.ues.dam235.sabortours.util.UbicacionGps;


public class HomeActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String[]> permisoUbicacion =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), r -> {
                if (UbicacionGps.tienePermiso(this)) actualizarUbicacion();
                else Toast.makeText(this, "Sin permiso de ubicación: usaremos Santa Tecla. "
                        + "Puedes activarlo en Ajustes para ver distancias reales.", Toast.LENGTH_LONG).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Baja los restaurantes de Firestore a Room; las pantallas se actualizan solas
        new RestauranteRepository(this).sincronizarRestaurantes();

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

        if (savedInstanceState == null) pedirUbicacion();   // solo al abrir, no al rotar
    }

    private void mostrar(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /** También la llama la etiqueta de ubicación de Inicio al tocarla. */
    public void pedirUbicacion() {
        if (UbicacionGps.tienePermiso(this)) actualizarUbicacion();
        else permisoUbicacion.launch(UbicacionGps.PERMISOS);
    }

    private void actualizarUbicacion() {
        UbicacionGps.actualizarGlobal(this, new UbicacionGps.Callback() {
            @Override public void ok(double lat, double lng) { }
            @Override public void error(String mensaje) {
                Toast.makeText(HomeActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }
}