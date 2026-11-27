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
import android.widget.ImageButton;
import edu.sv.ues.dam235.sabortours.data.repository.FavoritoRepository;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import edu.sv.ues.dam235.sabortours.data.local.Resena;
import edu.sv.ues.dam235.sabortours.data.repository.ResenaRepository;

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

        // ---- Favoritos ----
        FavoritoRepository favRepo = new FavoritoRepository(this);
        favRepo.sincronizar();
        ImageButton btnFavorito = findViewById(R.id.btnFavorito);
        final boolean[] esFavorito = {false};

        favRepo.esFavorito(restauranteId).observe(this, cantidad -> {
            esFavorito[0] = cantidad != null && cantidad > 0;
            btnFavorito.setImageResource(esFavorito[0]
                    ? R.drawable.ic_favorito_on : R.drawable.ic_favorito_off);
            btnFavorito.setContentDescription(esFavorito[0]
                    ? "Quitar de favoritos" : "Marcar como favorito");
        });
        btnFavorito.setOnClickListener(v -> favRepo.alternar(restauranteId, esFavorito[0]));

        // ---- Reseñas ----
        ResenaRepository resRepo = new ResenaRepository(this);
        resRepo.sincronizar(restauranteId);

        RecyclerView rvResenas = findViewById(R.id.rvResenas);
        rvResenas.setLayoutManager(new LinearLayoutManager(this));
        rvResenas.setNestedScrollingEnabled(false);
        ResenaAdapter resenaAdapter = new ResenaAdapter();
        rvResenas.setAdapter(resenaAdapter);

        TextView tvSinResenas = findViewById(R.id.tvSinResenas);
        MaterialButton btnResena = findViewById(R.id.btnEscribirResena);
        final Resena[] mia = {null};

        resRepo.resenas(restauranteId).observe(this, lista -> {
            resenaAdapter.setLista(lista);
            tvSinResenas.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        });
        resRepo.miResena(restauranteId).observe(this, r -> {
            mia[0] = r;
            btnResena.setText(r == null ? "Escribir reseña" : "Editar mi reseña");
        });
        btnResena.setOnClickListener(v -> mostrarDialogoResena(resRepo, restauranteId, mia[0]));

        repo.restaurante(restauranteId).observe(this, r -> {
            if (r == null) return;
            String tipo = r.tipoComida != null && !r.tipoComida.isEmpty() ? r.tipoComida : r.categoria;
            tvNombre.setText(r.nombre);
            tvInfo.setText(tipo + " · " + r.precio);
            tvDireccion.setText(r.direccion);
            tvDesc.setText(r.descripcion);
            tvRating.setText(r.numResenas == 0
                    ? "Sin reseñas aún"
                    : String.format(Locale.US, "⭐ %.1f · %d %s", r.rating, r.numResenas,
                    r.numResenas == 1 ? "reseña" : "reseñas"));

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

    private void mostrarDialogoResena(ResenaRepository repo, String restauranteId, Resena actual) {
        View vista = getLayoutInflater().inflate(R.layout.dialog_resena, null);
        RatingBar rb = vista.findViewById(R.id.rbCalificacion);
        EditText et = vista.findViewById(R.id.etComentario);
        if (actual != null) {
            rb.setRating(actual.calificacion);
            et.setText(actual.comentario);
        }

        AlertDialog dialogo = new MaterialAlertDialogBuilder(this)
                .setTitle(actual == null ? "Tu reseña" : "Editar tu reseña")
                .setView(vista)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Publicar", null)   // se define abajo para no cerrar si hay error
                .create();
        dialogo.show();

        Button btnOk = dialogo.getButton(AlertDialog.BUTTON_POSITIVE);
        btnOk.setOnClickListener(v -> {
            int estrellas = Math.round(rb.getRating());
            if (estrellas < 1) {
                Toast.makeText(this, "Elige de 1 a 5 estrellas", Toast.LENGTH_SHORT).show();
                return;
            }
            btnOk.setEnabled(false);
            repo.publicar(restauranteId, estrellas, et.getText().toString().trim(),
                    new ResenaRepository.Resultado() {
                        @Override public void ok() {
                            dialogo.dismiss();
                            Toast.makeText(DetalleRestauranteActivity.this,
                                    "¡Gracias por tu reseña!", Toast.LENGTH_SHORT).show();
                        }
                        @Override public void error(String mensaje) {
                            btnOk.setEnabled(true);
                            Toast.makeText(DetalleRestauranteActivity.this, mensaje, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}