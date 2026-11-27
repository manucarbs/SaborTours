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
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import com.bumptech.glide.Glide;
import edu.sv.ues.dam235.sabortours.util.ImagenUtil;
import android.app.Activity;
import android.content.Intent;
import edu.sv.ues.dam235.sabortours.util.UbicacionGps;
import edu.sv.ues.dam235.sabortours.util.Categorias;
import android.view.LayoutInflater;   // si no está
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import edu.sv.ues.dam235.sabortours.util.Categorias;
import edu.sv.ues.dam235.sabortours.util.Lista;

public class ConfigNegocioFragment extends Fragment {

    private static final List<String> PRECIOS = Arrays.asList("$", "$$", "$$$");

    private TextInputEditText etNombre, etTipo, etDireccion, etCorreo, etTelefono, etCapacidad, etDescripcion, etLat, etLng;
    private Spinner spPrecio;
    private ChipGroup chipCategorias;
    private TextInputEditText etEtiquetas;
    private MaterialButton btnApertura, btnCierre;
    private ChipGroup chipPlan;

    private Restaurante actual;          // el negocio tal como está en Room
    private boolean cargado = false;     // para llenar el formulario solo una vez
    private String apertura = "09:00";
    private String cierre = "21:00";
    private ImageView ivFoto;
    private TextView tvFotoInfo, tvQuitarFoto;
    private MaterialButton btnFoto;
    private boolean fotoCambiada = false;

    private final ActivityResultLauncher<PickVisualMediaRequest> selectorFoto =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) procesarFoto(uri);
            });

    private final ActivityResultLauncher<String[]> permisoUbicacion =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), r -> {
                if (UbicacionGps.tienePermiso(requireContext())) usarMiUbicacion();
                else Toast.makeText(getContext(), "Sin permiso de ubicación. "
                        + "Puedes elegir el punto en el mapa.", Toast.LENGTH_LONG).show();
            });

    private final ActivityResultLauncher<Intent> selectorMapa =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
                Intent d = res.getData();
                if (res.getResultCode() == Activity.RESULT_OK && d != null)
                    ponerCoordenadas(d.getDoubleExtra("lat", 0), d.getDoubleExtra("lng", 0));
            });

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
        etEtiquetas = v.findViewById(R.id.etCfgEtiquetas);
        chipCategorias = v.findViewById(R.id.chipCfgCategorias);
        armarChipsCategorias(inflater);
        spPrecio = v.findViewById(R.id.spCfgPrecio);
        btnApertura = v.findViewById(R.id.btnCfgApertura);
        btnCierre = v.findViewById(R.id.btnCfgCierre);
        chipPlan = v.findViewById(R.id.chipPlan);

        ivFoto = v.findViewById(R.id.ivCfgFoto);
        tvFotoInfo = v.findViewById(R.id.tvCfgFotoInfo);
        tvQuitarFoto = v.findViewById(R.id.tvCfgQuitarFoto);
        btnFoto = v.findViewById(R.id.btnCfgFoto);
        btnFoto.setOnClickListener(x -> selectorFoto.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
        tvQuitarFoto.setOnClickListener(x -> {
            if (actual == null) return;
            actual.imagenUrl = null;
            fotoCambiada = true;
            tvFotoInfo.setText("Foto quitada. Pulsa Guardar para aplicar el cambio.");
            mostrarFoto();
        });

        spPrecio.setAdapter(adaptador(PRECIOS));

        btnApertura.setOnClickListener(x -> elegirHora(true));
        btnCierre.setOnClickListener(x -> elegirHora(false));
        v.findViewById(R.id.btnCfgGuardar).setOnClickListener(x -> guardar());
        v.findViewById(R.id.btnCerrarSesion).setOnClickListener(x -> Sesion.cerrar(requireActivity()));

        v.findViewById(R.id.btnCfgUsarGps).setOnClickListener(x -> {
            if (UbicacionGps.tienePermiso(requireContext())) usarMiUbicacion();
            else permisoUbicacion.launch(UbicacionGps.PERMISOS);
        });
        v.findViewById(R.id.btnCfgElegirMapa).setOnClickListener(x -> elegirEnMapa());

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

        // Categorías del negocio; si es un registro viejo, se usa su categoría única
        List<String> ids = new ArrayList<>();
        for (String v : Lista.separar(r.categorias)) {
            Categorias.Categoria c = Categorias.buscar(v);
            if (c != null) ids.add(c.id);
        }
        if (ids.isEmpty()) {
            Categorias.Categoria c = Categorias.buscar(r.categoria);
            if (c != null) ids.add(c.id);
        }
        for (int i = 0; i < chipCategorias.getChildCount(); i++) {
            Chip chip = (Chip) chipCategorias.getChildAt(i);
            chip.setChecked(ids.contains((String) chip.getTag()));
        }
        etEtiquetas.setText(r.etiquetas == null ? "" : r.etiquetas.replace("|", ", "));

        int pre = PRECIOS.indexOf(r.precio);
        spPrecio.setSelection(Math.max(pre, 0));

        if (r.apertura != null) apertura = r.apertura;
        if (r.cierre != null) cierre = r.cierre;
        mostrarHoras();

        if ("premium".equals(r.plan)) chipPlan.check(R.id.chipPremium);
        else if ("standard".equals(r.plan)) chipPlan.check(R.id.chipStandard);
        else chipPlan.check(R.id.chipBasico);
        mostrarFoto();
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

    private void mostrarFoto() {
        if (!isAdded() || getView() == null) return;
        if (actual == null || actual.imagenUrl == null) {
            Glide.with(this).clear(ivFoto);
            ivFoto.setImageResource(android.R.drawable.ic_menu_gallery);
            tvQuitarFoto.setVisibility(View.GONE);
        } else {
            Glide.with(this).load(actual.imagenUrl).into(ivFoto);
            tvQuitarFoto.setVisibility(View.VISIBLE);
        }
    }

    private void procesarFoto(Uri uri) {
        if (actual == null) {
            Toast.makeText(getContext(), "Aún se están cargando los datos", Toast.LENGTH_SHORT).show();
            return;
        }
        btnFoto.setEnabled(false);
        tvFotoInfo.setText("Procesando foto…");
        ImagenUtil.procesar(requireContext(), uri, new ImagenUtil.Resultado() {
            @Override public void ok(String dataUri, int bytes) {
                if (!isAdded() || getView() == null) return;
                actual.imagenUrl = dataUri;
                fotoCambiada = true;
                mostrarFoto();
                tvFotoInfo.setText("✔ Foto lista (" + (bytes / 1024) + " KB). Pulsa Guardar para publicarla.");
                btnFoto.setEnabled(true);
            }
            @Override public void error(String mensaje) {
                if (!isAdded() || getView() == null) return;
                tvFotoInfo.setText("");
                btnFoto.setEnabled(true);
                Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void usarMiUbicacion() {
        Toast.makeText(getContext(), "Buscando tu ubicación…", Toast.LENGTH_SHORT).show();
        UbicacionGps.obtener(requireContext(), true, new UbicacionGps.Callback() {
            @Override public void ok(double lat, double lng) {
                if (isAdded()) ponerCoordenadas(lat, lng);
            }
            @Override public void error(String mensaje) {
                if (isAdded()) Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void elegirEnMapa() {
        Intent i = new Intent(requireContext(), SeleccionarUbicacionActivity.class);
        Double lat = leerDouble(etLat), lng = leerDouble(etLng);
        if (lat != null && lng != null) {      // si ya había un punto, el mapa abre ahí
            i.putExtra("hay", true);
            i.putExtra("lat", lat);
            i.putExtra("lng", lng);
        }
        selectorMapa.launch(i);
    }

    private void ponerCoordenadas(double lat, double lng) {
        etLat.setText(String.format(Locale.US, "%.6f", lat));
        etLng.setText(String.format(Locale.US, "%.6f", lng));
        Toast.makeText(getContext(), "Ubicación lista. Pulsa Guardar para publicarla.", Toast.LENGTH_LONG).show();
    }

    private static Double leerDouble(TextInputEditText e) {
        try {
            return Double.parseDouble(texto(e).replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void guardar() {
        List<String> idsCategorias = idsMarcados();
        if (idsCategorias.isEmpty()) {
            Toast.makeText(getContext(), "Elige al menos una categoría", Toast.LENGTH_SHORT).show();
            return;
        }

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
        actual.categorias = Lista.unir(idsCategorias);
        actual.categoria = Categorias.buscar(idsCategorias.get(0)).nombre;   // la principal: la primera
        actual.etiquetas = Lista.unir(etiquetasEscritas());
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

        new NegocioRepository(requireContext()).guardarNegocio(actual, fotoCambiada);
        fotoCambiada = false;
        Toast.makeText(getContext(), "Cambios guardados", Toast.LENGTH_SHORT).show();
    }

    // Un chip por categoría del catálogo; no deja marcar más de 3
    private void armarChipsCategorias(LayoutInflater inflater) {
        for (Categorias.Categoria c : Categorias.TODAS) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_chip_categoria, chipCategorias, false);
            chip.setId(View.generateViewId());
            chip.setText(c.nombre);
            chip.setTag(c.id);
            chip.setOnCheckedChangeListener((b, marcado) -> {
                if (marcado && idsMarcados().size() > Categorias.MAX_POR_NEGOCIO) {
                    b.setChecked(false);
                    Toast.makeText(getContext(), "Máximo " + Categorias.MAX_POR_NEGOCIO
                            + " categorías", Toast.LENGTH_SHORT).show();
                }
            });
            chipCategorias.addView(chip);
        }
    }

    private List<String> idsMarcados() {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < chipCategorias.getChildCount(); i++) {
            Chip chip = (Chip) chipCategorias.getChildAt(i);
            if (chip.isChecked()) ids.add((String) chip.getTag());
        }
        return ids;
    }

    // "pupusas, Desayunos" -> ["pupusas", "Desayunos"]: sin repetidas, máx. 8 de hasta 30 letras
    private List<String> etiquetasEscritas() {
        List<String> res = new ArrayList<>();
        for (String parte : texto(etEtiquetas).split(",")) {
            String e = parte.replace("|", "").trim();
            if (e.isEmpty() || e.length() > 30 || res.size() >= 8) continue;
            boolean repetida = false;
            for (String x : res) if (x.equalsIgnoreCase(e)) repetida = true;
            if (!repetida) res.add(e);
        }
        return res;
    }
}