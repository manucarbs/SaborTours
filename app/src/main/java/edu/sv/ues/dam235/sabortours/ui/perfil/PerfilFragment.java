package edu.sv.ues.dam235.sabortours.ui.perfil;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.local.AppDatabase;
import edu.sv.ues.dam235.sabortours.data.local.Usuario;
import edu.sv.ues.dam235.sabortours.util.Sesion;

public class PerfilFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        TextView tvNombre = view.findViewById(R.id.tvPerfilNombre);
        TextView tvCorreo = view.findViewById(R.id.tvPerfilCorreo);
        TextView tvTelefono = view.findViewById(R.id.tvPerfilTelefono);
        view.findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> Sesion.cerrar(requireActivity()));

        Context app = requireContext().getApplicationContext();
        String uid = FirebaseAuth.getInstance().getUid();
        AppDatabase.io.execute(() -> {
            Usuario u = uid == null ? null : AppDatabase.get(app).usuarioDao().buscarPorId(uid);
            if (u == null || getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                tvNombre.setText(u.nombre);
                tvCorreo.setText(u.correo);
                tvTelefono.setText(u.telefono == null || u.telefono.isEmpty() ? "No registrado" : u.telefono);
            });
        });
        return view;
    }
}