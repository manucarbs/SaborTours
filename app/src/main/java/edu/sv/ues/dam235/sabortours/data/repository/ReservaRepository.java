package edu.sv.ues.dam235.sabortours.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.sv.ues.dam235.sabortours.data.local.AppDatabase;
import edu.sv.ues.dam235.sabortours.data.local.Reserva;
import edu.sv.ues.dam235.sabortours.data.local.ReservaDao;
import edu.sv.ues.dam235.sabortours.util.Formatos;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.ListenerRegistration;

public class ReservaRepository {

    public interface Resultado {
        void ok();
        void error(String mensaje);
    }

    public interface CuposListener {
        /** ocupadosPorHora: "19:00" -> personas ya reservadas. Es null si no se pudo consultar. */
        void listo(Map<String, Long> ocupadosPorHora);
    }

    // Errores "esperados" (sin cupo, reserva ya cambiada...) con un mensaje listo para el usuario
    private static class ReservaException extends RuntimeException {
        ReservaException(String mensaje) { super(mensaje); }
    }

    private final ReservaDao dao;
    private final AppDatabase db;
    private final FirebaseFirestore fs = FirebaseFirestore.getInstance();

    public ReservaRepository(Context context) {
        this.db = AppDatabase.get(context);
        this.dao = db.reservaDao();
    }

    private static String uid() { return FirebaseAuth.getInstance().getUid(); }

    // ---- Lectura: siempre desde Room ----
    public LiveData<List<Reserva>> proximas() { return dao.proximas(uid(), Formatos.hoyIso()); }
    public LiveData<List<Reserva>> historial() { return dao.historial(uid(), Formatos.hoyIso()); }
    public LiveData<List<Reserva>> proximasDeMiNegocio() { return dao.proximasDeNegocio(uid(), Formatos.hoyIso()); }

    // ---- Sincronización: Firestore -> Room ----
    public void sincronizarMisReservas() {
        String uid = uid();
        if (uid == null) return;
        fs.collection("reservas").whereEqualTo("usuarioId", uid).get().addOnSuccessListener(snap -> {
            List<Reserva> lista = aLista(snap.getDocuments());
            AppDatabase.io.execute(() -> reemplazar(() -> dao.borrarDeCliente(uid), lista));
        });
    }

    // Escucha en vivo las reservas del negocio y las copia a Room. Hay que cancelarla con remove().
    public ListenerRegistration escucharReservasDeMiNegocio() {
        String uid = uid();
        if (uid == null) return null;
        return fs.collection("reservas").whereEqualTo("restauranteId", uid)
                .addSnapshotListener((snap, error) -> {
                    if (snap == null) return;   // sin conexión: se sigue con la copia local
                    List<Reserva> lista = aLista(snap.getDocuments());
                    AppDatabase.io.execute(() -> reemplazar(() -> dao.borrarDeNegocio(uid), lista));
                });
    }

    // ---- Cupos ocupados de un día (para mostrar la disponibilidad de cada franja) ----
    public void cuposDelDia(String restauranteId, String fecha, CuposListener listener) {
        fs.collection("restaurantes").document(restauranteId).collection("cupos")
                .whereGreaterThanOrEqualTo(FieldPath.documentId(), fecha + "_")
                .whereLessThan(FieldPath.documentId(), fecha + "_\uf8ff")   // todos los ids que empiezan con "fecha_"
                .get()
                .addOnSuccessListener(snap -> {
                    Map<String, Long> mapa = new HashMap<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String hora = d.getId().substring(fecha.length() + 1);
                        Long oc = d.getLong("ocupados");
                        mapa.put(hora, oc == null ? 0L : oc);
                    }
                    listener.listo(mapa);
                })
                .addOnFailureListener(e -> listener.listo(null));
    }

    private void reemplazar(Runnable borrar, List<Reserva> lista) {
        db.runInTransaction(() -> {
            borrar.run();
            dao.guardarTodas(lista);
        });
    }

    // ---- Crear reserva: transacción que verifica el cupo de la franja ----
    public void crear(String restauranteId, String restauranteNombre, String clienteNombre,
                      String fecha, String hora, int personas, Resultado cb) {
        String uid = uid();
        DocumentReference restRef = fs.collection("restaurantes").document(restauranteId);
        DocumentReference cupoRef = restRef.collection("cupos").document(fecha + "_" + hora);
        DocumentReference resRef = fs.collection("reservas").document();   // id generado aquí

        Map<String, Object> datos = new HashMap<>();
        datos.put("usuarioId", uid);
        datos.put("restauranteId", restauranteId);
        datos.put("restauranteNombre", restauranteNombre);
        datos.put("clienteNombre", clienteNombre);
        datos.put("fecha", fecha);
        datos.put("hora", hora);
        datos.put("personas", personas);
        datos.put("estado", "pendiente");

        fs.runTransaction(tx -> {
            // Todas las lecturas van antes que las escrituras
            DocumentSnapshot rest = tx.get(restRef);
            if (!rest.exists()) throw new ReservaException("Este restaurante ya no está disponible");
            Long cap = rest.getLong("capacidad");
            long capacidad = cap == null ? 20 : cap;

            DocumentSnapshot cupo = tx.get(cupoRef);
            Long oc = cupo.getLong("ocupados");
            long ocupados = oc == null ? 0 : oc;

            if (ocupados + personas > capacidad) {
                long libres = Math.max(0, capacidad - ocupados);
                throw new ReservaException(libres == 0
                        ? "Esa hora ya no tiene cupo. Prueba con otra."
                        : "Solo quedan " + libres + " lugares a esa hora.");
            }

            Map<String, Object> nuevoCupo = new HashMap<>();
            nuevoCupo.put("ocupados", ocupados + personas);
            tx.set(cupoRef, nuevoCupo);
            tx.set(resRef, datos);
            return null;
        }).addOnSuccessListener(x -> {
            Reserva r = new Reserva();
            r.id = resRef.getId();
            r.usuarioId = uid;
            r.restauranteId = restauranteId;
            r.restauranteNombre = restauranteNombre;
            r.clienteNombre = clienteNombre;
            r.fecha = fecha;
            r.hora = hora;
            r.personas = personas;
            r.estado = "pendiente";
            AppDatabase.io.execute(() -> dao.guardar(r));
            cb.ok();
        }).addOnFailureListener(e -> cb.error(mensaje(e)));
    }

    // ---- Cambiar estado: el cliente cancela; el dueño acepta o rechaza ----
    // Cancelar y rechazar devuelven el cupo a la franja.
    public void cambiarEstado(String reservaId, String nuevoEstado, Resultado cb) {
        DocumentReference resRef = fs.collection("reservas").document(reservaId);

        fs.runTransaction(tx -> {
            DocumentSnapshot res = tx.get(resRef);
            if (!res.exists()) throw new ReservaException("La reserva ya no existe");

            String actual = res.getString("estado");
            boolean libera = "cancelada".equals(nuevoEstado) || "rechazada".equals(nuevoEstado);

            if ("cancelada".equals(nuevoEstado)) {
                if (!"pendiente".equals(actual) && !"aceptada".equals(actual))
                    throw new ReservaException("Esta reserva ya no se puede cancelar");
            } else if (!"pendiente".equals(actual)) {
                throw new ReservaException("Esta reserva ya fue " + actual);
            }

            DocumentReference cupoRef = null;
            DocumentSnapshot cupo = null;
            long personas = 0;
            if (libera) {
                Long p = res.getLong("personas");
                personas = p == null ? 0 : p;
                cupoRef = fs.collection("restaurantes").document(res.getString("restauranteId"))
                        .collection("cupos").document(res.getString("fecha") + "_" + res.getString("hora"));
                cupo = tx.get(cupoRef);       // lectura antes de escribir
            }

            tx.update(resRef, "estado", nuevoEstado);
            if (libera && cupo.exists()) {
                Long oc = cupo.getLong("ocupados");
                long ocupados = oc == null ? 0 : oc;
                tx.update(cupoRef, "ocupados", Math.max(0, ocupados - personas));
            }
            return null;
        }).addOnSuccessListener(x -> {
            AppDatabase.io.execute(() -> dao.actualizarEstado(reservaId, nuevoEstado));
            cb.ok();
        }).addOnFailureListener(e -> cb.error(mensaje(e)));
    }

    // ---- Utilidades ----
    private static String mensaje(Exception e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof ReservaException) return t.getMessage();
        }
        if (e instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException.Code c = ((FirebaseFirestoreException) e).getCode();
            if (c == FirebaseFirestoreException.Code.UNAVAILABLE)
                return "Sin conexión. Las reservas necesitan internet para verificar el cupo.";
            if (c == FirebaseFirestoreException.Code.PERMISSION_DENIED)
                return "No tienes permiso para realizar esta acción.";
        }
        return "No se pudo completar la operación. Intenta de nuevo.";
    }

    private static List<Reserva> aLista(List<DocumentSnapshot> docs) {
        List<Reserva> lista = new ArrayList<>();
        for (DocumentSnapshot d : docs) {
            Reserva r = new Reserva();
            r.id = d.getId();
            r.usuarioId = d.getString("usuarioId");
            r.restauranteId = d.getString("restauranteId");
            r.restauranteNombre = d.getString("restauranteNombre");
            r.clienteNombre = d.getString("clienteNombre");
            r.fecha = d.getString("fecha");
            r.hora = d.getString("hora");
            Long p = d.getLong("personas");
            r.personas = p == null ? 1 : p.intValue();
            r.estado = d.getString("estado");
            lista.add(r);
        }
        return lista;
    }
}