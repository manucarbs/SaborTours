package edu.sv.ues.dam235.sabortours.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import edu.sv.ues.dam235.sabortours.data.local.AppDatabase;
import edu.sv.ues.dam235.sabortours.data.local.Usuario;
import edu.sv.ues.dam235.sabortours.ui.HomeActivity;
import edu.sv.ues.dam235.sabortours.ui.LoginActivity;
import edu.sv.ues.dam235.sabortours.ui.negocio.NegocioActivity;

public class Sesion {

    // Convierte el documento de Firestore en un Usuario local
    public static Usuario deDocumento(String uid, DocumentSnapshot doc) {
        Usuario u = new Usuario();
        u.id = uid;
        u.nombre = doc.getString("nombre");
        u.correo = doc.getString("correo");
        u.telefono = doc.getString("telefono");
        u.rol = doc.getString("rol");
        return u;
    }

    // Guarda el perfil en Room y abre la pantalla que corresponde al rol
    public static void entrar(Activity a, Usuario u) {
        Context app = a.getApplicationContext();
        AppDatabase.io.execute(() -> {
            AppDatabase.get(app).usuarioDao().guardar(u);
            a.runOnUiThread(() -> irAInicio(a, u.rol));
        });
    }

    public static void irAInicio(Activity a, String rol) {
        Class<?> destino = "comerciante".equals(rol) ? NegocioActivity.class : HomeActivity.class;
        a.startActivity(new Intent(a, destino));
        a.finish();
    }

    public static void cerrar(Activity a) {
        Context app = a.getApplicationContext();
        FirebaseAuth.getInstance().signOut();
        AppDatabase.io.execute(() -> {
            AppDatabase.get(app).clearAllTables();   // no deja datos de este usuario en el teléfono
            a.runOnUiThread(() -> {
                Intent i = new Intent(a, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                a.startActivity(i);
            });
        });
    }
}