package edu.sv.ues.dam235.sabortours.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import edu.sv.ues.dam235.sabortours.data.local.AppDatabase;
import edu.sv.ues.dam235.sabortours.data.local.Usuario;
import edu.sv.ues.dam235.sabortours.util.Sesion;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
        if (fu == null) { irALogin(); return; }

        String uid = fu.getUid();
        Context app = getApplicationContext();
        AppDatabase.io.execute(() -> {
            Usuario local = AppDatabase.get(app).usuarioDao().buscarPorId(uid);
            if (local != null) {
                runOnUiThread(() -> Sesion.irAInicio(this, local.rol));
                return;
            }
            // Hay sesión pero no hay copia local: se lee el perfil de Firestore
            runOnUiThread(() -> FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) Sesion.entrar(this, Sesion.deDocumento(uid, doc));
                        else irALogin();
                    })
                    .addOnFailureListener(e -> irALogin()));
        });
    }

    private void irALogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}