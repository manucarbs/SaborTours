package edu.sv.ues.dam235.sabortours.ui;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.local.Resena;

public class ResenaAdapter extends RecyclerView.Adapter<ResenaAdapter.ViewHolder> {

    private List<Resena> lista = new ArrayList<>();

    public void setLista(List<Resena> nueva) {
        this.lista = nueva;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resena, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Resena r = lista.get(position);
        h.tvAutor.setText(r.autorNombre);

        StringBuilder estrellas = new StringBuilder();
        for (int i = 1; i <= 5; i++) estrellas.append(i <= r.calificacion ? "★" : "☆");
        h.tvEstrellas.setText(estrellas);

        boolean hayComentario = r.comentario != null && !r.comentario.isEmpty();
        h.tvComentario.setText(r.comentario);
        h.tvComentario.setVisibility(hayComentario ? View.VISIBLE : View.GONE);

        if (r.fechaMillis > 0) {
            h.tvFecha.setText(DateFormat.getMediumDateFormat(h.itemView.getContext())
                    .format(new Date(r.fechaMillis)));
            h.tvFecha.setVisibility(View.VISIBLE);
        } else {
            h.tvFecha.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAutor, tvEstrellas, tvComentario, tvFecha;

        ViewHolder(@NonNull View v) {
            super(v);
            tvAutor = v.findViewById(R.id.tvResenaAutor);
            tvEstrellas = v.findViewById(R.id.tvResenaEstrellas);
            tvComentario = v.findViewById(R.id.tvResenaComentario);
            tvFecha = v.findViewById(R.id.tvResenaFecha);
        }
    }
}