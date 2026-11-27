package edu.sv.ues.dam235.sabortours.ui.reservas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.local.Reserva;
import edu.sv.ues.dam235.sabortours.util.Formatos;

public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.ViewHolder> {

    public interface OnCancelar { void cancelar(Reserva reserva); }

    private List<Reserva> lista = new ArrayList<>();
    private boolean puedeCancelar = true;
    private final OnCancelar listener;

    public ReservaAdapter(OnCancelar listener) { this.listener = listener; }

    public void setLista(List<Reserva> nueva, boolean puedeCancelar) {
        this.lista = nueva;
        this.puedeCancelar = puedeCancelar;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reserva, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Reserva r = lista.get(position);
        h.tvNombre.setText(r.restauranteNombre);
        h.tvHora.setText(Formatos.hora12(r.hora));
        h.tvFecha.setText(Formatos.fechaBonita(r.fecha));
        h.tvPersonas.setText(r.personas + (r.personas == 1 ? " persona" : " personas"));
        h.tvEstado.setText(r.estado.substring(0, 1).toUpperCase() + r.estado.substring(1));

        h.btnCancelar.setVisibility(puedeCancelar ? View.VISIBLE : View.GONE);
        h.btnCancelar.setOnClickListener(v -> listener.cancelar(r));
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvHora, tvFecha, tvPersonas, tvEstado;
        MaterialButton btnCancelar;

        ViewHolder(@NonNull View v) {
            super(v);
            tvNombre = v.findViewById(R.id.tvResNombre);
            tvHora = v.findViewById(R.id.tvResHora);
            tvFecha = v.findViewById(R.id.tvResFecha);
            tvPersonas = v.findViewById(R.id.tvResPersonas);
            tvEstado = v.findViewById(R.id.tvResEstado);
            btnCancelar = v.findViewById(R.id.btnCancelarReserva);
        }
    }
}