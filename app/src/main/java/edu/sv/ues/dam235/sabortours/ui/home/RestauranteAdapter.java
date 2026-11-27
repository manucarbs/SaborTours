package edu.sv.ues.dam235.sabortours.ui.home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.local.Restaurante;
import edu.sv.ues.dam235.sabortours.ui.DetalleRestauranteActivity;
import edu.sv.ues.dam235.sabortours.util.Ubicacion;

public class RestauranteAdapter extends RecyclerView.Adapter<RestauranteAdapter.ViewHolder> {

    private List<Restaurante> lista = new ArrayList<>();

    public void setLista(List<Restaurante> nueva) {
        this.lista = nueva;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurante, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Restaurante r = lista.get(position);
        String tipo = r.tipoComida != null && !r.tipoComida.isEmpty() ? r.tipoComida : r.categoria;

        h.tvNombre.setText(r.nombre);
        h.tvCategoria.setText(tipo + " · " + r.precio);
        h.tvRating.setText(r.numResenas == 0
                ? "Sin reseñas aún"
                : String.format(Locale.US, "⭐ %.1f · %d reseñas", r.rating, r.numResenas));

        Double km = Ubicacion.distanciaKm(r.latitud, r.longitud);
        if (km != null) {
            h.tvDistancia.setText(km + " km de distancia");
            h.tvDistancia.setVisibility(View.VISIBLE);
        } else {
            h.tvDistancia.setVisibility(View.GONE);
        }

        Glide.with(h.itemView.getContext()).load(r.imagenUrl)
                .placeholder(android.R.drawable.ic_menu_gallery).into(h.ivImagen);

        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetalleRestauranteActivity.class);
            intent.putExtra("id", r.id);   // el detalle lee todo lo demás de Room
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImagen;
        TextView tvNombre, tvCategoria, tvRating, tvDistancia;

        ViewHolder(@NonNull View v) {
            super(v);
            ivImagen = v.findViewById(R.id.ivRestaurante);
            tvNombre = v.findViewById(R.id.tvNombre);
            tvCategoria = v.findViewById(R.id.tvCategoria);
            tvRating = v.findViewById(R.id.tvRating);
            tvDistancia = v.findViewById(R.id.tvDistancia);
        }
    }
}