package edu.sv.ues.dam235.sabortours.ui.negocio;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.local.Restaurante;
import edu.sv.ues.dam235.sabortours.data.repository.NegocioRepository;
import edu.sv.ues.dam235.sabortours.util.Formatos;
import edu.sv.ues.dam235.sabortours.util.Sesion;
import edu.sv.ues.dam235.sabortours.util.Validador;

public class ConfigNegocioFragment extends Fragment {

    private static final List<String> CATEGORIAS = Arrays.asList("Típica", "Hamburguesas", "Pizza", "Cafés");
    private static final List<String> PRECIOS = Arrays.asList("$", "$$", "$$$");

    private TextInputEditText etNombre, etTipo, etDireccion, etCorreo, etTelefono, etCapacidad, etDescripcion, etLat, etLng;
    private Spinner spCategoria, spPrecio;
    private MaterialButton btnApertura, btnCierre;
    private ChipGroup chipPlan;

    private Restaurante actual;          // el negocio tal como está en Room
    private boolean cargado = false;     // para llenar el formulario solo una vez
    private String apertura = "09:00";
    private String cierre = "21:00";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_config_negocio, container, false);

        etNombre = v.findViewById(R.id.etCfgNombre);
        etTipo = v.findViewById(R.id.etCfgTipo);
        etDireccion = v.findViewById(R.id.etCfgDireccion);
        etCorreo = v.findViewById(R.id.etCfgCorreo);
        etTelefono = v.findViewById(R.id.etCfgTelefono);
        etCapacidad = v.findViewById(R.id.etCfgCapacidad);
        etDescripcion = v.findViewById(R.id.etCfgDescripcion);
        etLat = v.findViewById(R.id.etCfgLat);
        etLng = v.findViewById(R.id.etCfgLng);
        spCategoria = v.findViewById(R.id.spCfgCategoria);
        spPrecio = v.findViewById(R.id.spCfgPrecio);
        btnApertura = v.findViewById(R.id.btnCfgApertura);
        btnCierre = v.findViewById(R.id.btnCfgCierre);
        chipPlan = v.findViewById(R.id.chipPlan);

        spCategoria.setAdapter(adaptador(CATEGORIAS));
        spPrecio.setAdapter(adaptador(PRECIOS));

        btnApertura.setOnClickListener(x -> elegirHora(true));
        btnCierre.setOnClickListener(x -> elegirHora(false));
        v.findViewById(R.id.btnCfgGuardar).setOnClickListener(x -> guardar());
        v.findViewById(R.id.btnCerrarSesion).setOnClickListener(x -> Sesion.cerrar(requireActivity()));

        // Se llena una sola vez: si Room cambia mientras escribes, no te borra lo que tecleaste
        new NegocioRepository(requireContext()).miNegocio().observe(getViewLifecycleOwner(), r -> {
            if (r == null || cargado) return;
            cargado = true;
            actual = r;
            llenar(r);
        });
        return v;
    }

    private ArrayAdapter<String> adaptador(List<String> items) {
        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return a;
    }

    private void llenar(Restaurante r) {
        etNombre.setText(r.nombre);
        etTipo.setText(r.tipoComida);
        etDireccion.setText(r.direccion);
        etCorreo.setText(r.correo);
        etTelefono.setText(r.telefono);
        etCapacidad.setText(String.valueOf(r.capacidad));
        etDescripcion.setText(r.descripcion);
        if (r.latitud != null) etLat.setText(String.valueOf(r.latitud));
        if (r.longitud != null) etLng.setText(String.valueOf(r.longitud));

        int cat = CATEGORIAS.indexOf(r.categoria);
        spCategoria.setSelection(Math.max(cat, 0));
        int pre = PRECIOS.indexOf(r.precio);
        spPrecio.setSelection(Math.max(pre, 0));

        if (r.apertura != null) apertura = r.apertura;
        if (r.cierre != null) cierre = r.cierre;
        mostrarHoras();

        if ("premium".equals(r.plan)) chipPlan.check(R.id.chipPremium);
        else if ("standard".equals(r.plan)) chipPlan.check(R.id.chipStandard);
        else chipPlan.check(R.id.chipBasico);
    }

    private void mostrarHoras() {
        btnApertura.setText(Formatos.hora12(apertura));
        btnCierre.setText(Formatos.hora12(cierre));
    }

    private void elegirHora(boolean esApertura) {
        String base = esApertura ? apertura : cierre;
        int h = Integer.parseInt(base.substring(0, 2));
        int m = Integer.parseInt(base.substring(3, 5));
        new TimePickerDialog(requireContext(), (picker, hh, mm) -> {
            String valor = String.format(Locale.US, "%02d:%02d", hh, mm);   // se guarda en 24 h
            if (esApertura) apertura = valor; else cierre = valor;
            mostrarHoras();
        }, h, m, false).show();
    }

    private static String texto(TextInputEditText e) {
        return String.valueOf(e.getText()).trim();
    }

    private void guardar() {
        if (actual == null) {
            Toast.makeText(getContext(), "Aún se están cargando los datos", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombre = texto(etNombre);
        if (nombre.length() < 2) { etNombre.setError("Escribe el nombre del negocio"); return; }

        String correo = texto(etCorreo);
        if (!correo.isEmpty() && !Validador.correoValido(correo)) { etCorreo.setError("Correo no válido"); return; }

        int capacidad;
        try {
            capacidad = Integer.parseInt(texto(etCapacidad));
        } catch (NumberFormatException e) {
            etCapacidad.setError("Escribe un número"); return;
        }
        if (capacidad < 1 || capacidad > 500) { etCapacidad.setError("Debe estar entre 1 y 500"); return; }

        if (apertura.equals(cierre)) {
            Toast.makeText(getContext(), "La apertura y el cierre no pueden ser iguales", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ubicación: o se llenan las dos coordenadas o ninguna
        Double lat = actual.latitud, lng = actual.longitud;
        String sLat = texto(etLat), sLng = texto(etLng);
        if (!sLat.isEmpty() || !sLng.isEmpty()) {
            try {
                lat = Double.parseDouble(sLat.replace(',', '.'));
                lng = Double.parseDouble(sLng.replace(',', '.'));
            } catch (NumberFormatException e) {
                etLat.setError("Escribe ambas coordenadas"); return;
            }
            if (Math.abs(lat) > 90) { etLat.setError("La latitud va de -90 a 90"); return; }
            if (Math.abs(lng) > 180) { etLng.setError("La longitud va de -180 a 180"); return; }
        }

        int planId = chipPlan.getCheckedChipId();
        String plan = planId == R.id.chipPremium ? "premium" : planId == R.id.chipStandard ? "standard" : "basico";

        actual.nombre = nombre;
        actual.tipoComida = texto(etTipo);
        actual.categoria = CATEGORIAS.get(spCategoria.getSelectedItemPosition());
        actual.precio = PRECIOS.get(spPrecio.getSelectedItemPosition());
        actual.direccion = texto(etDireccion);
        actual.correo = correo;
        actual.telefono = texto(etTelefono);
        actual.descripcion = texto(etDescripcion);
        actual.apertura = apertura;
        actual.cierre = cierre;
        actual.capacidad = capacidad;
        actual.latitud = lat;
        actual.longitud = lng;
        actual.plan = plan;

        new NegocioRepository(requireContext()).guardarNegocio(actual);
        Toast.makeText(getContext(), "Cambios guardados", Toast.LENGTH_SHORT).show();
    }
}