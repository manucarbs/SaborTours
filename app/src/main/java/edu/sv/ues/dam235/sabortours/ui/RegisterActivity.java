package edu.sv.ues.dam235.sabortours.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.HashMap;
import java.util.Map;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.local.Usuario;
import edu.sv.ues.dam235.sabortours.util.Sesion;
import edu.sv.ues.dam235.sabortours.util.Validador;

public class RegisterActivity extends AppCompatActivity {

    private static final String[] CATEGORIAS = {"Típica", "Hamburguesas", "Pizza", "Cafés"};

    private TextInputEditText etNombre, etCorreo, etTelefono, etPassword, etPassword2, etNegocio, etDireccion;
    private CheckBox cbNegocio;
    private View layoutNegocio;
    private Spinner spCategoria;
    private MaterialButton btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etTelefono = findViewById(R.id.etTelefono);
        etPassword = findViewById(R.id.etPassword);
        etPassword2 = findViewById(R.id.etPassword2);
        etNegocio = findViewById(R.id.etNegocio);
        etDireccion = findViewById(R.id.etDireccion);
        cbNegocio = findViewById(R.id.cbNegocio);
        layoutNegocio = findViewById(R.id.layoutNegocio);
        spCategoria = findViewById(R.id.spCategoria);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, CATEGORIAS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(adapter);

        // Los campos del negocio solo aparecen si marca la casilla
        cbNegocio.setOnCheckedChangeListener((b, marcado) ->
                layoutNegocio.setVisibility(marcado ? View.VISIBLE : View.GONE));

        btnRegistrar.setOnClickListener(v -> registrar());
        findViewById(R.id.tvVolverLogin).setOnClickListener(v -> finish());
    }

    private static String texto(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void registrar() {
        String nombre = texto(etNombre);
        String correo = texto(etCorreo);
        String telefono = texto(etTelefono);
        String pass = etPassword.getText() == null ? "" : etPassword.getText().toString();
        String pass2 = etPassword2.getText() == null ? "" : etPassword2.getText().toString();
        boolean esNegocio = cbNegocio.isChecked();
        String nombreNegocio = texto(etNegocio);

        if (!Validador.nombreValido(nombre)) { etNombre.setError("Escribe tu nombre (mínimo 3 letras)"); return; }
        if (!Validador.correoValido(correo)) { etCorreo.setError("Correo no válido"); return; }
        if (!Validador.passwordValida(pass)) { etPassword.setError("Mínimo 8 caracteres"); return; }
        if (!pass.equals(pass2)) { etPassword2.setError("Las contraseñas no coinciden"); return; }
        if (esNegocio && nombreNegocio.length() < 2) { etNegocio.setError("Escribe el nombre del negocio"); return; }

        btnRegistrar.setEnabled(false);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(correo, pass)
                .addOnSuccessListener(res -> guardarPerfil(res.getUser(), nombre, correo, telefono, esNegocio, nombreNegocio))
                .addOnFailureListener(e -> fallo(mensajeAuth(e)));
    }

    // Crea el perfil (y el negocio, si aplica) en UN solo paso: o se guarda todo o nada
    private void guardarPerfil(FirebaseUser user, String nombre, String correo, String telefono,
                               boolean esNegocio, String nombreNegocio) {
        String uid = user.getUid();
        String rol = esNegocio ? "comerciante" : "cliente";
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        Map<String, Object> perfil = new HashMap<>();
        perfil.put("nombre", nombre);
        perfil.put("correo", correo);
        perfil.put("telefono", telefono);
        perfil.put("rol", rol);
        batch.set(db.collection("usuarios").document(uid), perfil);

        if (esNegocio) {
            String categoria = CATEGORIAS[spCategoria.getSelectedItemPosition()];
            Map<String, Object> negocio = new HashMap<>();
            negocio.put("duenoUid", uid);
            negocio.put("nombre", nombreNegocio);
            negocio.put("categoria", categoria);
            negocio.put("tipoComida", categoria);
            negocio.put("descripcion", "");
            negocio.put("precio", "$$");
            negocio.put("direccion", texto(etDireccion));
            negocio.put("telefono", telefono);
            negocio.put("correo", correo);
            negocio.put("apertura", "09:00");
            negocio.put("cierre", "21:00");
            negocio.put("capacidad", 20);
            negocio.put("plan", "basico");
            negocio.put("rating", 0);
            negocio.put("numResenas", 0);
            batch.set(db.collection("restaurantes").document(uid), negocio);
        }

        batch.commit()
                .addOnSuccessListener(v -> {
                    Usuario u = new Usuario();
                    u.id = uid;
                    u.nombre = nombre;
                    u.correo = correo;
                    u.telefono = telefono;
                    u.rol = rol;
                    Sesion.entrar(this, u);
                })
                .addOnFailureListener(e -> {
                    user.delete(); // no dejar una cuenta sin perfil
                    fallo("No se pudo crear el perfil: " + e.getMessage());
                });
    }

    private String mensajeAuth(Exception e) {
        if (e instanceof FirebaseAuthUserCollisionException) return "Ese correo ya está registrado";
        if (e instanceof FirebaseAuthWeakPasswordException) return "La contraseña es muy débil";
        if (e instanceof FirebaseNetworkException) return "Sin conexión a internet";
        return "No se pudo crear la cuenta: " + e.getMessage();
    }

    private void fallo(String mensaje) {
        btnRegistrar.setEnabled(true);
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }
}