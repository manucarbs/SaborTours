package edu.sv.ues.dam235.sabortours.ui.negocio;

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

public class ProductoNegocioAdapter extends RecyclerView.Adapter<ProductoNegocioAdapter.ViewHolder> {

    public interface Acciones {
        void editar(Producto producto);
        void eliminar(Producto producto);
    }

    private List<Producto> lista = new ArrayList<>();
    private final Acciones acciones;

    public ProductoNegocioAdapter(Acciones acciones) { this.acciones = acciones; }

    public void setLista(List<Producto> nueva) {
        this.lista = nueva;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto_negocio, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Producto p = lista.get(position);
        h.tvNombre.setText(p.nombre);
        h.tvPrecio.setText(String.format(Locale.US, "$%.2f", p.precio));
        h.tvEstado.setVisibility(p.disponible ? View.GONE : View.VISIBLE);
        Glide.with(h.itemView.getContext()).load(p.imagenUrl)
                .placeholder(android.R.drawable.ic_menu_gallery).into(h.ivImagen);
        h.btnEditar.setOnClickListener(v -> acciones.editar(p));
        h.btnEliminar.setOnClickListener(v -> acciones.eliminar(p));
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImagen;
        TextView tvNombre, tvPrecio, tvEstado;
        View btnEditar, btnEliminar;

        ViewHolder(@NonNull View v) {
            super(v);
            ivImagen = v.findViewById(R.id.ivProdNegocio);
            tvNombre = v.findViewById(R.id.tvProdNombre);
            tvPrecio = v.findViewById(R.id.tvProdPrecio);
            tvEstado = v.findViewById(R.id.tvProdEstado);
            btnEditar = v.findViewById(R.id.btnProdEditar);
            btnEliminar = v.findViewById(R.id.btnProdEliminar);
        }
    }
}