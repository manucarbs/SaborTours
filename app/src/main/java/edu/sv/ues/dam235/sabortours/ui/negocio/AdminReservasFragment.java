package edu.sv.ues.dam235.sabortours.ui.negocio;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.ListenerRegistration;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.local.Reserva;
import edu.sv.ues.dam235.sabortours.data.repository.ReservaRepository;

public class AdminReservasFragment extends Fragment {

    private ReservaRepository repo;
    private ListenerRegistration escucha;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_reservas, container, false);
        repo = new ReservaRepository(requireContext());
        Context app = requireContext().getApplicationContext();

        RecyclerView rv = view.findViewById(R.id.rvReservasNegocio);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        ReservaNegocioAdapter adapter = new ReservaNegocioAdapter(new ReservaNegocioAdapter.Acciones() {
            @Override
            public void aceptar(Reserva r) {
                repo.cambiarEstado(r.id, "aceptada", resultado(app, "Reserva aceptada"));
            }

            @Override
            public void rechazar(Reserva r) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Rechazar reserva")
                        .setMessage("¿Rechazar la reserva de " + r.clienteNombre + "? El lugar quedará libre otra vez.")
                        .setPositiveButton("Rechazar", (d, w) ->
                                repo.cambiarEstado(r.id, "rechazada", resultado(app, "Reserva rechazada")))
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });
        rv.setAdapter(adapter);

        View tvVacio = view.findViewById(R.id.tvSinReservasNegocio);
        repo.proximasDeMiNegocio().observe(getViewLifecycleOwner(), lista -> {
            adapter.setLista(lista);
            tvVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        });
        return view;
    }

    // Mientras la pantalla está a la vista, las reservas nuevas llegan solas
    @Override
    public void onStart() {
        super.onStart();
        escucha = repo.escucharReservasDeMiNegocio();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (escucha != null) { escucha.remove(); escucha = null; }
    }

    private static ReservaRepository.Resultado resultado(Context app, String mensajeOk) {
        return new ReservaRepository.Resultado() {
            @Override public void ok() { Toast.makeText(app, mensajeOk, Toast.LENGTH_SHORT).show(); }
            @Override public void error(String mensaje) { Toast.makeText(app, mensaje, Toast.LENGTH_LONG).show(); }
        };
    }
}