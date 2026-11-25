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
import java.util.List;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.model.Restaurante;
import edu.sv.ues.dam235.sabortours.ui.DetalleRestauranteActivity;

public class RestauranteAdapter extends RecyclerView.Adapter<RestauranteAdapter.ViewHolder> {
    private List<Restaurante> lista;
    public RestauranteAdapter(List<Restaurante> lista) { this.lista = lista; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurante, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurante r = lista.get(position);
        holder.tvNombre.setText(r.getNombre());
        holder.tvCategoria.setText(r.getCategoria() + " · " + r.getPrecio());
        holder.tvRating.setText("⭐ " + r.getRating());
        Glide.with(holder.itemView.getContext()).load(r.getImagen_url()).placeholder(android.R.drawable.ic_menu_gallery).into(holder.ivImagen);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetalleRestauranteActivity.class);
            intent.putExtra("id", r.getId());
            intent.putExtra("nombre", r.getNombre());
            intent.putExtra("categoria", r.getCategoria());
            intent.putExtra("precio", r.getPrecio());
            intent.putExtra("rating", r.getRating());
            intent.putExtra("imagen", r.getImagen_url());
            intent.putExtra("descripcion", r.getDescripcion());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImagen;
        TextView tvNombre, tvCategoria, tvRating;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImagen = itemView.findViewById(R.id.ivRestaurante);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }
}