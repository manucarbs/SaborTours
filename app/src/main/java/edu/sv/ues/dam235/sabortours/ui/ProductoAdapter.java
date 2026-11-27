package edu.sv.ues.dam235.sabortours.ui;

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
import edu.sv.ues.dam235.sabortours.data.local.Producto;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ViewHolder> {

    private List<Producto> lista = new ArrayList<>();

    public void setLista(List<Producto> nueva) {
        this.lista = nueva;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Producto p = lista.get(position);
        h.tvNombre.setText(p.nombre);
        h.tvPrecio.setText(String.format(Locale.US, "$%.2f", p.precio));
        Glide.with(h.itemView.getContext()).load(p.imagenUrl)
                .placeholder(android.R.drawable.ic_menu_gallery).into(h.ivProducto);
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProducto;
        TextView tvNombre, tvPrecio;

        ViewHolder(@NonNull View v) {
            super(v);
            ivProducto = v.findViewById(R.id.ivProducto);
            tvNombre = v.findViewById(R.id.tvProductoNombre);
            tvPrecio = v.findViewById(R.id.tvProductoPrecio);
        }
    }
}