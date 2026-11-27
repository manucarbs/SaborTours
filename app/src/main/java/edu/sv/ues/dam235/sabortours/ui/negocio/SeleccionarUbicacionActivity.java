package edu.sv.ues.dam235.sabortours.ui.negocio;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.util.Ubicacion;

/** Permite al negocio tocar el mapa para elegir dónde está. Devuelve "lat" y "lng". */
public class SeleccionarUbicacionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mapa;
    private Marker marcador;
    private LatLng elegido;
    private LatLng inicial;
    private boolean hayPunto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar_ubicacion);

        hayPunto = getIntent().getBooleanExtra("hay", false);
        inicial = new LatLng(
                getIntent().getDoubleExtra("lat", Ubicacion.lat),
                getIntent().getDoubleExtra("lng", Ubicacion.lng));

        SupportMapFragment fragmento = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapaSeleccion);
        if (fragmento != null) fragmento.getMapAsync(this);

        findViewById(R.id.btnConfirmarUbicacion).setOnClickListener(v -> {
            if (elegido == null) {
                Toast.makeText(this, "Toca el mapa para elegir un punto", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent resultado = new Intent();
            resultado.putExtra("lat", elegido.latitude);
            resultado.putExtra("lng", elegido.longitude);
            setResult(RESULT_OK, resultado);
            finish();
        });
    }

    @Override
    public void onMapReady(GoogleMap g) {
        mapa = g;
        mapa.getUiSettings().setZoomControlsEnabled(true);
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(inicial, 16f));
        if (hayPunto) colocar(inicial);
        mapa.setOnMapClickListener(this::colocar);
        mapa.setOnMapLongClickListener(this::colocar);
    }

    private void colocar(LatLng punto) {
        elegido = punto;
        if (marcador == null) marcador = mapa.addMarker(new MarkerOptions().position(punto));
        else marcador.setPosition(punto);
    }
}