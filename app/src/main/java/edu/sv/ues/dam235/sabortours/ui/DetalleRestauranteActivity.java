package edu.sv.ues.dam235.sabortours.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import edu.sv.ues.dam235.sabortours.R;

public class DetalleRestauranteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_restaurante);

        int restauranteId = getIntent().getIntExtra("id", 0);
        String nombre = getIntent().getStringExtra("nombre");
        String categoria = getIntent().getStringExtra("categoria");
        String precio = getIntent().getStringExtra("precio");
        double rating = getIntent().getDoubleExtra("rating", 0);
        String imagen = getIntent().getStringExtra("imagen");
        String descripcion = getIntent().getStringExtra("descripcion");

        TextView tvNombre = findViewById(R.id.tvDetalleNombre);
        TextView tvInfo = findViewById(R.id.tvDetalleInfo);
        TextView tvRating = findViewById(R.id.tvDetalleRating);
        TextView tvDesc = findViewById(R.id.tvDetalleDescripcion);
        ImageView ivImagen = findViewById(R.id.ivDetalleImagen);

        tvNombre.setText(nombre);
        tvInfo.setText(categoria + " · " + precio);
        tvRating.setText("⭐ " + rating + " · 124 reseñas");
        tvDesc.setText(descripcion);
        Glide.with(this).load(imagen).placeholder(android.R.drawable.ic_menu_gallery).into(ivImagen);

        MaterialButton btnReservar = findViewById(R.id.btnReservarMesa);
        btnReservar.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReservarActivity.class);
            intent.putExtra("restaurante_id", restauranteId);
            intent.putExtra("restaurante_nombre", nombre);
            startActivity(intent);
        });
    }
}