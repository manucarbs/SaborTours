package edu.sv.ues.dam235.sabortours.ui.negocio;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.repository.RestauranteRepository;

public class NegocioActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_negocio);

        // Baja a Room los datos más recientes del negocio y de su menú
        RestauranteRepository repo = new RestauranteRepository(this);
        repo.sincronizarRestaurantes();
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) repo.sincronizarProductos(uid);

        BottomNavigationView nav = findViewById(R.id.bottom_navigation_negocio);

        if (savedInstanceState == null) mostrar(new NegocioFragment());

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment f;
            if (id == R.id.nav_negocio) f = new NegocioFragment();
            else if (id == R.id.nav_admin_reservas) f = new AdminReservasFragment();
            else if (id == R.id.nav_config) f = new ConfigNegocioFragment();
            else return false;
            mostrar(f);
            return true;
        });
    }

    private void mostrar(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_negocio, f)
                .commit();
    }
}