package edu.sv.ues.dam235.sabortours.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.sv.ues.dam235.sabortours.data.local.AppDatabase;
import edu.sv.ues.dam235.sabortours.data.local.Resena;
import edu.sv.ues.dam235.sabortours.data.local.Usuario;
import edu.sv.ues.dam235.sabortours.util.Calificacion;

public class ResenaRepository {

    public interface Resultado {
        void ok();
        void error(String mensaje);
    }

    // Errores "esperados" con un mensaje listo para el usuario
    private static class ResenaException extends RuntimeException {
        ResenaException(String mensaje) { super(mensaje); }
    }

    private final AppDatabase db;
    private final FirebaseFirestore fs = FirebaseFirestore.getInstance();

    public ResenaRepository(Context context) {
        this.db = AppDatabase.get(context);
    }

    private static String uid() { return FirebaseAuth.getInstance().getUid(); }

    // ---- Lectura: siempre desde Room ----
    public LiveData<List<Resena>> resenas(String restauranteId) {
        return db.resenaDao().deRestaurante(restauranteId);
    }

    public LiveData<Resena> miResena(String restauranteId) {
        return db.resenaDao().deUsuario(restauranteId, uid());
    }

    // ---- Sincronización: Firestore -> Room ----
    public void sincronizar(String restauranteId) {
        fs.collection("restaurantes").document(restauranteId).collection("resenas").get()
                .addOnSuccessListener(snap -> {
                    List<Resena> lista = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Resena r = new Resena();
                        r.restauranteId = restauranteId;
                        r.usuarioId = d.getId();
                        r.autorNombre = d.getString("autorNombre");
                        Long c = d.getLong("calificacion");
                        r.calificacion = c == null ? 0 : c.intValue();
                        r.comentario = d.getString("comentario");
                        Timestamp t = d.getTimestamp("fecha");
                        r.fechaMillis = t == null ? 0 : t.toDate().getTime();
                        lista.add(r);
                    }
                    AppDatabase.io.execute(() -> db.runInTransaction(() -> {
                        db.resenaDao().borrarDeRestaurante(restauranteId);
                        db.resenaDao().guardarTodas(lista);
                    }));
                });
    }

    // ---- Publicar o editar mi reseña ----
    public void publicar(String restauranteId, int calificacion, String comentario, Resultado cb) {
        String uid = uid();
        if (uid == null) { cb.error("Tu sesión ya no es válida. Vuelve a iniciar sesión."); return; }

        // El nombre del autor sale del perfil guardado en Room (hilo de fondo)
        AppDatabase.io.execute(() -> {
            Usuario u = db.usuarioDao().buscarPorId(uid);
            String nombre = (u == null || u.nombre == null || u.nombre.trim().isEmpty())
                    ? "Cliente" : u.nombre.trim();
            if (nombre.length() > 100) nombre = nombre.substring(0, 100);
            enviar(restauranteId, uid, nombre, calificacion, comentario, cb);
        });
    }

    private void enviar(String restauranteId, String uid, String nombre,
                        int calificacion, String comentario, Resultado cb) {
        DocumentReference restRef = fs.collection("restaurantes").document(restauranteId);
        DocumentReference resRef = restRef.collection("resenas").document(uid);

        fs.runTransaction(tx -> {
            // Todas las lecturas antes que las escrituras
            DocumentSnapshot rest = tx.get(restRef);
            if (!rest.exists()) throw new ResenaException("Este restaurante ya no está disponible");
            DocumentSnapshot previa = tx.get(resRef);

            Long totalPrevio = rest.getLong("numResenas");
            long total = totalPrevio == null ? 0 : totalPrevio;
            Double ratingPrevio = rest.getDouble("rating");
            double rating = ratingPrevio == null ? 0 : ratingPrevio;

            Calificacion.Resumen nuevo;
            if (previa.exists()) {
                Long anterior = previa.getLong("calificacion");
                if (anterior == null || total < 1)
                    throw new ResenaException("No se pudo leer tu reseña anterior");
                nuevo = Calificacion.conEditada(rating, total, anterior.intValue(), calificacion);
            } else {
                nuevo = Calificacion.conNueva(rating, total, calificacion);
            }

            Map<String, Object> datos = new HashMap<>();
            datos.put("usuarioId", uid);
            datos.put("autorNombre", nombre);
            datos.put("calificacion", calificacion);
            datos.put("comentario", comentario);
            datos.put("fecha", FieldValue.serverTimestamp());

            tx.set(resRef, datos);
            tx.update(restRef, "rating", nuevo.rating, "numResenas", nuevo.total);
            return new double[]{nuevo.rating, nuevo.total};
        }).addOnSuccessListener(res -> {
            Resena local = new Resena();
            local.restauranteId = restauranteId;
            local.usuarioId = uid;
            local.autorNombre = nombre;
            local.calificacion = calificacion;
            local.comentario = comentario;
            local.fechaMillis = System.currentTimeMillis();
            AppDatabase.io.execute(() -> db.runInTransaction(() -> {
                db.resenaDao().guardar(local);
                db.restauranteDao().actualizarRating(restauranteId, res[0], (int) res[1]);
            }));
            cb.ok();
        }).addOnFailureListener(e -> cb.error(mensaje(e)));
    }

    private static String mensaje(Exception e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof ResenaException) return t.getMessage();
        }
        if (e instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException.Code c = ((FirebaseFirestoreException) e).getCode();
            if (c == FirebaseFirestoreException.Code.UNAVAILABLE)
                return "Sin conexión. Publicar una reseña necesita internet.";
            if (c == FirebaseFirestoreException.Code.PERMISSION_DENIED)
                return "No tienes permiso para publicar esta reseña.";
        }
        return "No se pudo publicar la reseña. Intenta de nuevo.";
    }
}