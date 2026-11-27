package edu.sv.ues.dam235.sabortours.ui.negocio;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.local.Reserva;
import edu.sv.ues.dam235.sabortours.util.Formatos;

public class ReservaNegocioAdapter extends RecyclerView.Adapter<ReservaNegocioAdapter.ViewHolder> {

    public interface Acciones {
        void aceptar(Reserva reserva);
        void rechazar(Reserva reserva);
    }

    private List<Reserva> lista = new ArrayList<>();
    private final Acciones acciones;

    public ReservaNegocioAdapter(Acciones acciones) { this.acciones = acciones; }

    public void setLista(List<Reserva> nueva) {
        this.lista = nueva;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reserva_negocio, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Reserva r = lista.get(position);
        h.tvCliente.setText(r.clienteNombre);
        h.tvHora.setText(Formatos.hora12(r.hora));
        h.tvFecha.setText(Formatos.fechaBonita(r.fecha));
        h.tvPersonas.setText(r.personas + (r.personas == 1 ? " persona" : " personas"));
        h.tvEstado.setText(r.estado.substring(0, 1).toUpperCase() + r.estado.substring(1));

        // Verde si está aceptada; gris si ya no cuenta (cancelada o rechazada); texto normal si espera respuesta
        int color = "aceptada".equals(r.estado) ? R.color.sabor_green_dark
                : "pendiente".equals(r.estado) ? R.color.sabor_black_text
                : R.color.sabor_gray_text;
        h.tvEstado.setTextColor(ContextCompat.getColor(h.itemView.getContext(), color));

        // Solo se responde a las pendientes
        h.filaBotones.setVisibility("pendiente".equals(r.estado) ? View.VISIBLE : View.GONE);
        h.btnAceptar.setOnClickListener(v -> acciones.aceptar(r));
        h.btnRechazar.setOnClickListener(v -> acciones.rechazar(r));
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCliente, tvHora, tvFecha, tvPersonas, tvEstado;
        View filaBotones;
        MaterialButton btnAceptar, btnRechazar;

        ViewHolder(@NonNull View v) {
            super(v);
            tvCliente = v.findViewById(R.id.tvRnCliente);
            tvHora = v.findViewById(R.id.tvRnHora);
            tvFecha = v.findViewById(R.id.tvRnFecha);
            tvPersonas = v.findViewById(R.id.tvRnPersonas);
            tvEstado = v.findViewById(R.id.tvRnEstado);
            filaBotones = v.findViewById(R.id.filaBotonesRn);
            btnAceptar = v.findViewById(R.id.btnRnAceptar);
            btnRechazar = v.findViewById(R.id.btnRnRechazar);
        }
    }
}