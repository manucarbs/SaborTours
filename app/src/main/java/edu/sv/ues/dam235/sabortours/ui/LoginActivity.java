package edu.sv.ues.dam235.sabortours.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.local.Usuario;
import edu.sv.ues.dam235.sabortours.util.Sesion;
import edu.sv.ues.dam235.sabortours.util.Validador;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private CheckBox cbPropietario;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbPropietario = findViewById(R.id.cbPropietario);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> iniciarSesion());
        findViewById(R.id.tvCrearCuenta).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void iniciarSesion() {
        String correo = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
        String pass = etPassword.getText() == null ? "" : etPassword.getText().toString();

        if (!Validador.correoValido(correo)) { etEmail.setError("Correo no válido"); return; }
        if (pass.isEmpty()) { etPassword.setError("Escribe tu contraseña"); return; }

        btnLogin.setEnabled(false);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(correo, pass)
                .addOnSuccessListener(res -> {
                    String uid = res.getUser().getUid();
                    FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
                            .addOnSuccessListener(doc -> {
                                if (!doc.exists()) {
                                    auth.signOut();
                                    fallo("No encontramos el perfil de esta cuenta");
                                    return;
                                }
                                Usuario u = Sesion.deDocumento(uid, doc);
                                boolean esNegocio = "comerciante".equals(u.rol);
                                if (esNegocio != cbPropietario.isChecked()) {
                                    auth.signOut();
                                    fallo(esNegocio
                                            ? "Esta es una cuenta de negocio: marca \"Soy propietario\""
                                            : "Esta es una cuenta de cliente: desmarca \"Soy propietario\"");
                                    return;
                                }
                                Sesion.entrar(this, u);
                            })
                            .addOnFailureListener(e -> {
                                auth.signOut();
                                fallo("No se pudo leer el perfil: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> fallo(e instanceof FirebaseNetworkException
                        ? "Sin conexión a internet"
                        : "Correo o contraseña incorrectos"));
    }

    private void fallo(String mensaje) {
        btnLogin.setEnabled(true);
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }
}
