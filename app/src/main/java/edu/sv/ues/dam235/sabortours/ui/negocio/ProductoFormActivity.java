package edu.sv.ues.dam235.sabortours.ui.negocio;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Locale;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.local.AppDatabase;
import edu.sv.ues.dam235.sabortours.data.local.Producto;
import edu.sv.ues.dam235.sabortours.data.repository.NegocioRepository;
import edu.sv.ues.dam235.sabortours.util.ImagenUtil;

public class ProductoFormActivity extends AppCompatActivity {

    private static final double PASO_PRECIO = 0.25;

    private TextInputEditText etNombre, etDescripcion, etPrecio;
    private MaterialSwitch swDisponible;
    private MaterialButton btnGuardar, btnFoto;
    private ImageView ivFoto;
    private TextView tvFotoInfo, tvQuitarFoto;

    private String productoId = "";
    private String imagenUrl = null;        // data URI de la foto actual (o null)
    private boolean fotoCambiada = false;   // solo si cambió se vuelve a enviar a Firestore

    // Selector de fotos del sistema: no requiere permisos
    private final ActivityResultLauncher<PickVisualMediaRequest> selectorFoto =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) procesarFoto(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_form);

        etNombre = findViewById(R.id.etProdNombre);
        etDescripcion = findViewById(R.id.etProdDescripcion);
        etPrecio = findViewById(R.id.etProdPrecio);
        swDisponible = findViewById(R.id.swDisponible);
        btnGuardar = findViewById(R.id.btnProdGuardar);
        btnFoto = findViewById(R.id.btnProdFoto);
        ivFoto = findViewById(R.id.ivProdFoto);
        tvFotoInfo = findViewById(R.id.tvProdFotoInfo);
        tvQuitarFoto = findViewById(R.id.tvProdQuitarFoto);

        // Si llega un id, es edición; si no, es un producto nuevo
        Intent in = getIntent();
        String id = in.getStringExtra("id");
        boolean esEdicion = id != null;
        ((TextView) findViewById(R.id.tvFormTitulo)).setText(esEdicion ? "Editar producto" : "Nuevo producto");
        btnGuardar.setText(esEdicion ? "Guardar cambios" : "Publicar");

        if (esEdicion) {
            productoId = id;
            etNombre.setText(in.getStringExtra("nombre"));
            etDescripcion.setText(in.getStringExtra("descripcion"));
            etPrecio.setText(String.format(Locale.US, "%.2f", in.getDoubleExtra("precio", 0)));
            swDisponible.setChecked(in.getBooleanExtra("disponible", true));

            // La foto pesa ~160 KB: no viaja por el Intent, se lee de Room
            btnGuardar.setEnabled(false);   // hasta cargar la foto actual, para no perderla al guardar
            AppDatabase.io.execute(() -> {
                String actual = AppDatabase.get(this).productoDao().imagenDe(id);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (!fotoCambiada) imagenUrl = actual;
                    mostrarFoto();
                    btnGuardar.setEnabled(true);
                });
            });
        } else {
            etPrecio.setText("1.00");
        }

        findViewById(R.id.tvFormVolver).setOnClickListener(v -> finish());
        findViewById(R.id.btnPrecioMenos).setOnClickListener(v -> cambiarPrecio(-PASO_PRECIO));
        findViewById(R.id.btnPrecioMas).setOnClickListener(v -> cambiarPrecio(PASO_PRECIO));
        btnFoto.setOnClickListener(v -> selectorFoto.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
        tvQuitarFoto.setOnClickListener(v -> {
            imagenUrl = null;
            fotoCambiada = true;
            tvFotoInfo.setText("");
            mostrarFoto();
        });
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void mostrarFoto() {
        if (imagenUrl == null) {
            Glide.with(this).clear(ivFoto);
            ivFoto.setImageResource(android.R.drawable.ic_menu_gallery);
            tvQuitarFoto.setVisibility(View.GONE);
        } else {
            Glide.with(this).load(imagenUrl).into(ivFoto);
            tvQuitarFoto.setVisibility(View.VISIBLE);
        }
    }

    private void procesarFoto(Uri uri) {
        btnFoto.setEnabled(false);
        tvFotoInfo.setText("Procesando foto…");
        ImagenUtil.procesar(this, uri, new ImagenUtil.Resultado() {
            @Override public void ok(String dataUri, int bytes) {
                if (isFinishing() || isDestroyed()) return;
                imagenUrl = dataUri;
                fotoCambiada = true;
                mostrarFoto();
                tvFotoInfo.setText("✔ Foto lista (" + (bytes / 1024) + " KB)");
                btnFoto.setEnabled(true);
            }
            @Override public void error(String mensaje) {
                if (isFinishing() || isDestroyed()) return;
                tvFotoInfo.setText("");
                btnFoto.setEnabled(true);
                Toast.makeText(ProductoFormActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private double leerPrecio() {
        try {
            return Double.parseDouble(String.valueOf(etPrecio.getText()).trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private void cambiarPrecio(double delta) {
        double p = leerPrecio();
        if (Double.isNaN(p)) p = 0;
        p = Math.max(0, Math.round((p + delta) * 100) / 100.0);
        etPrecio.setText(String.format(Locale.US, "%.2f", p));
    }

    private void guardar() {
        String nombre = String.valueOf(etNombre.getText()).trim();
        double precio = leerPrecio();

        if (nombre.length() < 2) { etNombre.setError("Escribe el nombre del producto"); return; }
        if (Double.isNaN(precio) || precio < 0 || precio > 10000) { etPrecio.setError("Precio no válido"); return; }

        Producto p = new Producto();
        p.id = productoId;                     // vacío = nuevo; el repositorio le asigna el id
        p.nombre = nombre;
        p.descripcion = String.valueOf(etDescripcion.getText()).trim();
        p.precio = Math.round(precio * 100) / 100.0;
        p.disponible = swDisponible.isChecked();
        p.imagenUrl = imagenUrl;

        new NegocioRepository(this).guardarProducto(p, fotoCambiada);
        Toast.makeText(this, productoId.isEmpty() ? "Producto publicado" : "Cambios guardados", Toast.LENGTH_SHORT).show();
        finish();
    }
}