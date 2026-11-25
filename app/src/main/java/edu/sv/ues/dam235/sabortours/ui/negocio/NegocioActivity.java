package edu.sv.ues.dam235.sabortours.ui.negocio;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.local.AppDatabase;
import edu.sv.ues.dam235.sabortours.data.local.Usuario;
import edu.sv.ues.dam235.sabortours.util.Sesion;

public class NegocioActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_negocio);

        TextView tvNombre = findViewById(R.id.tvNegocioNombre);
        findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> Sesion.cerrar(this));

        Context app = getApplicationContext();
        String uid = FirebaseAuth.getInstance().getUid();
        AppDatabase.io.execute(() -> {
            Usuario u = uid == null ? null : AppDatabase.get(app).usuarioDao().buscarPorId(uid);
            if (u != null) runOnUiThread(() -> tvNombre.setText("Hola, " + u.nombre));
        });
    }
}