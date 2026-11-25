package edu.sv.ues.dam235.sabortours.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import java.util.Locale;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.repository.RestauranteRepository;
import edu.sv.ues.dam235.sabortours.util.Formatos;
import edu.sv.ues.dam235.sabortours.util.Horario;

public class DetalleRestauranteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_restaurante);

        String restauranteId = getIntent().getStringExtra("id");
        if (restauranteId == null) { finish(); return; }

        TextView tvNombre = findViewById(R.id.tvDetalleNombre);
        TextView tvInfo = findViewById(R.id.tvDetalleInfo);
        TextView tvEstado = findViewById(R.id.tvDetalleEstado);
        TextView tvDireccion = findViewById(R.id.tvDetalleDireccion);
        TextView tvRating = findViewById(R.id.tvDetalleRating);
        TextView tvDesc = findViewById(R.id.tvDetalleDescripcion);
        TextView tvMenuVacio = findViewById(R.id.tvMenuVacio);
        ImageView ivImagen = findViewById(R.id.ivDetalleImagen);
        MaterialButton btnReservar = findViewById(R.id.btnReservarMesa);

        RecyclerView rvMenu = findViewById(R.id.rvMenu);
        rvMenu.setLayoutManager(new GridLayoutManager(this, 2));
        rvMenu.setNestedScrollingEnabled(false);
        ProductoAdapter adapter = new ProductoAdapter();
        rvMenu.setAdapter(adapter);

        RestauranteRepository repo = new RestauranteRepository(this);
        repo.sincronizarProductos(restauranteId);   // baja el menú más reciente

        repo.restaurante(restauranteId).observe(this, r -> {
            if (r == null) return;
            String tipo = r.tipoComida != null && !r.tipoComida.isEmpty() ? r.tipoComida : r.categoria;
            tvNombre.setText(r.nombre);
            tvInfo.setText(tipo + " · " + r.precio);
            tvDireccion.setText(r.direccion);
            tvDesc.setText(r.descripcion);
            tvRating.setText(r.numResenas == 0
                    ? "Sin reseñas aún"
                    : String.format(Locale.US, "⭐ %.1f · %d reseñas", r.rating, r.numResenas));

            boolean abierto = Horario.abiertoAhora(r.apertura, r.cierre);
            tvEstado.setText(abierto
                    ? "Abierto · Cierra " + Formatos.hora12(r.cierre)
                    : "Cerrado · Abre " + Formatos.hora12(r.apertura));
            tvEstado.setTextColor(ContextCompat.getColor(this,
                    abierto ? R.color.sabor_green_dark : R.color.sabor_gray_text));

            Glide.with(this).load(r.imagenUrl).placeholder(android.R.drawable.ic_menu_gallery).into(ivImagen);

            btnReservar.setOnClickListener(v -> {
                Intent intent = new Intent(this, ReservarActivity.class);
                intent.putExtra("restaurante_id", r.id);
                intent.putExtra("restaurante_nombre", r.nombre);
                startActivity(intent);
            });
        });

        repo.productos(restauranteId).observe(this, lista -> {
            adapter.setLista(lista);
            tvMenuVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}