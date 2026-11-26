package edu.sv.ues.dam235.sabortours.ui.negocio;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Locale;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.local.Producto;
import edu.sv.ues.dam235.sabortours.data.repository.NegocioRepository;

public class ProductoFormActivity extends AppCompatActivity {

    private static final double PASO_PRECIO = 0.25;

    private TextInputEditText etNombre, etDescripcion, etPrecio;
    private MaterialSwitch swDisponible;
    private String productoId = "";
    private String imagenUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_form);

        etNombre = findViewById(R.id.etProdNombre);
        etDescripcion = findViewById(R.id.etProdDescripcion);
        etPrecio = findViewById(R.id.etProdPrecio);
        swDisponible = findViewById(R.id.swDisponible);
        MaterialButton btnGuardar = findViewById(R.id.btnProdGuardar);

        // Si llega un id, es edición; si no, es un producto nuevo
        Intent in = getIntent();
        String id = in.getStringExtra("id");
        boolean esEdicion = id != null;
        ((TextView) findViewById(R.id.tvFormTitulo)).setText(esEdicion ? "Editar producto" : "Nuevo producto");
        btnGuardar.setText(esEdicion ? "Guardar cambios" : "Publicar");

        if (esEdicion) {
            productoId = id;
            imagenUrl = in.getStringExtra("imagen");
            etNombre.setText(in.getStringExtra("nombre"));
            etDescripcion.setText(in.getStringExtra("descripcion"));
            etPrecio.setText(String.format(Locale.US, "%.2f", in.getDoubleExtra("precio", 0)));
            swDisponible.setChecked(in.getBooleanExtra("disponible", true));
        } else {
            etPrecio.setText("1.00");
        }

        findViewById(R.id.tvFormVolver).setOnClickListener(v -> finish());
        findViewById(R.id.btnPrecioMenos).setOnClickListener(v -> cambiarPrecio(-PASO_PRECIO));
        findViewById(R.id.btnPrecioMas).setOnClickListener(v -> cambiarPrecio(PASO_PRECIO));
        btnGuardar.setOnClickListener(v -> guardar());
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
        p.imagenUrl = imagenUrl;               // se conserva la foto, si ya tenía

        new NegocioRepository(this).guardarProducto(p);
        Toast.makeText(this, productoId.isEmpty() ? "Producto publicado" : "Cambios guardados", Toast.LENGTH_SHORT).show();
        finish();
    }
}