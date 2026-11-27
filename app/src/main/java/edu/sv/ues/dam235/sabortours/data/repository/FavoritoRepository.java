package edu.sv.ues.dam235.sabortours.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.sv.ues.dam235.sabortours.data.local.AppDatabase;
import edu.sv.ues.dam235.sabortours.data.local.Favorito;
import edu.sv.ues.dam235.sabortours.data.local.FavoritoDao;

public class FavoritoRepository {

    private final AppDatabase db;
    private final FavoritoDao dao;
    private final FirebaseFirestore fs = FirebaseFirestore.getInstance();

    public FavoritoRepository(Context context) {
        this.db = AppDatabase.get(context);
        this.dao = db.favoritoDao();
    }

    private static String uid() { return FirebaseAuth.getInstance().getUid(); }

    private static Favorito nuevo(String uid, String rid) {
        Favorito f = new Favorito();
        f.usuarioId = uid;
        f.restauranteId = rid;
        return f;
    }

    private DocumentReference ref(String uid, String rid) {
        return fs.collection("usuarios").document(uid).collection("favoritos").document(rid);
    }

    // ---- Lectura: siempre desde Room ----
    public LiveData<Integer> esFavorito(String restauranteId) {
        return dao.contar(uid(), restauranteId);
    }

    public LiveData<List<String>> idsFavoritos() {
        return dao.ids(uid());
    }

    // ---- Sincronización: Firestore -> Room ----
    public void sincronizar() {
        String uid = uid();
        if (uid == null) return;
        fs.collection("usuarios").document(uid).collection("favoritos").get()
                .addOnSuccessListener(snap -> {
                    List<Favorito> lista = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) lista.add(nuevo(uid, d.getId()));
                    AppDatabase.io.execute(() -> db.runInTransaction(() -> {
                        dao.borrarDeUsuario(uid);
                        dao.guardarTodos(lista);
                    }));
                });
    }

    // ---- Escritura: primero Room (la pantalla reacciona ya), luego Firestore ----
    // Si Firestore rechaza la operación, se deshace el cambio local.
    public void alternar(String restauranteId, boolean eraFavorito) {
        String uid = uid();
        if (uid == null) return;

        if (eraFavorito) {
            AppDatabase.io.execute(() -> dao.borrar(uid, restauranteId));
            ref(uid, restauranteId).delete().addOnFailureListener(e ->
                    AppDatabase.io.execute(() -> dao.guardar(nuevo(uid, restauranteId))));
        } else {
            AppDatabase.io.execute(() -> dao.guardar(nuevo(uid, restauranteId)));
            Map<String, Object> datos = new HashMap<>();
            datos.put("restauranteId", restauranteId);
            datos.put("creado", FieldValue.serverTimestamp());
            ref(uid, restauranteId).set(datos).addOnFailureListener(e ->
                    AppDatabase.io.execute(() -> dao.borrar(uid, restauranteId)));
        }
    }
}