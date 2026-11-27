package edu.sv.ues.dam235.sabortours.ui.reservas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.local.Reserva;
import android.content.Context;
import android.widget.Toast;
import edu.sv.ues.dam235.sabortours.data.repository.ReservaRepository;

public class ReservasFragment extends Fragment {

    private ReservaRepository repo;
    private ReservaAdapter adapter;
    private TextView tvSinReservas;
    private LiveData<List<Reserva>> fuente;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservas, container, false);

        repo = new ReservaRepository(requireContext());
        repo.sincronizarMisReservas();
        tvSinReservas = view.findViewById(R.id.tvSinReservas);

        RecyclerView rv = view.findViewById(R.id.rvReservas);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReservaAdapter(this::confirmarCancelacion);
        rv.setAdapter(adapter);

        MaterialButtonToggleGroup toggle = view.findViewById(R.id.toggleReservas);
        toggle.addOnButtonCheckedListener((grupo, id, marcado) -> {
            if (marcado) mostrar(id == R.id.btnProximas);
        });
        toggle.check(R.id.btnProximas); // dispara mostrar(true)

        return view;
    }

    // Cambia entre "Próximas" e "Historial" observando la consulta que corresponde
    private void mostrar(boolean proximas) {
        if (fuente != null) fuente.removeObservers(getViewLifecycleOwner());

        fuente = proximas ? repo.proximas() : repo.historial();

        fuente.observe(getViewLifecycleOwner(), lista -> {
            adapter.setLista(lista, proximas);
            tvSinReservas.setText(proximas ? "No tienes reservas próximas" : "Aún no tienes historial");
            tvSinReservas.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void confirmarCancelacion(Reserva reserva) {
        Context app = requireContext().getApplicationContext();
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cancelar reserva")
                .setMessage("¿Quieres cancelar tu reserva en " + reserva.restauranteNombre + "?")
                .setPositiveButton("Sí, cancelar", (d, w) ->
                        repo.cambiarEstado(reserva.id, "cancelada", new ReservaRepository.Resultado() {
                            @Override public void ok() {
                                Toast.makeText(app, "Reserva cancelada", Toast.LENGTH_SHORT).show();
                            }
                            @Override public void error(String mensaje) {
                                Toast.makeText(app, mensaje, Toast.LENGTH_LONG).show();
                            }
                        }))
                .setNegativeButton("No", null)
                .show();
    }
}