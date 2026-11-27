package edu.sv.ues.dam235.sabortours.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import java.util.List;
import java.util.Locale;

/** Obtiene la ubicación del teléfono y la comparte con las pantallas. */
public class UbicacionGps {

    public interface Callback {
        void ok(double lat, double lng);      // se llama en el hilo principal
        void error(String mensaje);
    }

    public static final String[] PERMISOS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final String MSG_SIN_GPS = "No se pudo obtener tu ubicación. Activa el GPS e intenta de nuevo.";

    // Suben de valor cada vez que cambia la ubicación global: las listas se reordenan al observarlo
    private static final MutableLiveData<Long> cambios = new MutableLiveData<>(0L);
    private static final MutableLiveData<String> etiqueta =
            new MutableLiveData<>("Santa Tecla, La Libertad (predeterminada)");

    public static LiveData<Long> cambios() { return cambios; }
    public static LiveData<String> etiqueta() { return etiqueta; }

    public static boolean tienePermiso(Context c) {
        return ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /** Pide un punto al GPS. Requiere el permiso ya concedido. */
    @SuppressLint("MissingPermission")
    public static void obtener(Context context, boolean precisa, Callback cb) {
        if (!tienePermiso(context)) { cb.error("Falta el permiso de ubicación"); return; }
        Context app = context.getApplicationContext();
        FusedLocationProviderClient cliente = LocationServices.getFusedLocationProviderClient(app);
        int prioridad = precisa ? Priority.PRIORITY_HIGH_ACCURACY : Priority.PRIORITY_BALANCED_POWER_ACCURACY;

        cliente.getCurrentLocation(prioridad, new CancellationTokenSource().getToken())
                .addOnSuccessListener(loc -> {
                    if (loc != null) { cb.ok(loc.getLatitude(), loc.getLongitude()); return; }
                    // Sin lectura fresca: se prueba con la última ubicación conocida
                    cliente.getLastLocation()
                            .addOnSuccessListener(ult -> {
                                if (ult != null) cb.ok(ult.getLatitude(), ult.getLongitude());
                                else cb.error(MSG_SIN_GPS);
                            })
                            .addOnFailureListener(e -> cb.error(MSG_SIN_GPS));
                })
                .addOnFailureListener(e -> cb.error(MSG_SIN_GPS));
    }

    /** Actualiza la ubicación global del cliente: la usan las distancias y el orden por cercanía. */
    public static void actualizarGlobal(Context context, Callback cb) {
        Context app = context.getApplicationContext();
        obtener(app, false, new Callback() {
            @Override public void ok(double lat, double lng) {
                Ubicacion.lat = lat;
                Ubicacion.lng = lng;
                cambios.setValue(cambios.getValue() + 1);
                etiquetar(app, lat, lng);
                if (cb != null) cb.ok(lat, lng);
            }
            @Override public void error(String mensaje) {
                if (cb != null) cb.error(mensaje);
            }
        });
    }

    // Convierte coordenadas en "Ciudad, Departamento" (necesita internet; si falla, texto genérico)
    @SuppressWarnings("deprecation")
    private static void etiquetar(Context app, double lat, double lng) {
        new Thread(() -> {
            String texto = "Tu ubicación actual";
            try {
                if (Geocoder.isPresent()) {
                    List<Address> lista = new Geocoder(app, Locale.forLanguageTag("es")).getFromLocation(lat, lng, 1);
                    if (lista != null && !lista.isEmpty()) {
                        Address a = lista.get(0);
                        String ciudad = a.getLocality() != null ? a.getLocality() : a.getSubAdminArea();
                        String zona = a.getAdminArea();
                        if (ciudad != null && zona != null) texto = ciudad + ", " + zona;
                        else if (ciudad != null) texto = ciudad;
                        else if (zona != null) texto = zona;
                    }
                }
            } catch (Exception ignorada) { }
            etiqueta.postValue(texto);
        }).start();
    }
}