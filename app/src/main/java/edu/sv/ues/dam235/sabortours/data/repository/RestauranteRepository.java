package edu.sv.ues.dam235.sabortours.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import edu.sv.ues.dam235.sabortours.data.local.AppDatabase;
import edu.sv.ues.dam235.sabortours.data.local.Producto;
import edu.sv.ues.dam235.sabortours.data.local.Restaurante;
import edu.sv.ues.dam235.sabortours.util.Lista;

public class RestauranteRepository {

    private final AppDatabase db;
    private final FirebaseFirestore fs = FirebaseFirestore.getInstance();

    public RestauranteRepository(Context context) {
        this.db = AppDatabase.get(context);
    }

    // ---- Lectura: SIEMPRE desde Room ----
    public LiveData<List<Restaurante>> restaurantes() { return db.restauranteDao().todos(); }
    public LiveData<Restaurante> restaurante(String id) { return db.restauranteDao().porId(id); }
    public LiveData<List<Producto>> productos(String restauranteId) { return db.productoDao().deRestaurante(restauranteId); }

    // ---- Sincronización: Firestore -> Room ----
    // Si no hay internet, el listener de error no hace nada y se sigue usando la copia local.
    public void sincronizarRestaurantes() {
        fs.collection("restaurantes").get().addOnSuccessListener(snap -> {
            List<Restaurante> lista = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) lista.add(aRestaurante(d));
            AppDatabase.io.execute(() -> db.runInTransaction(() -> {
                db.restauranteDao().borrarTodos();
                db.restauranteDao().guardarTodos(lista);
            }));
        });
    }

    public void sincronizarProductos(String restauranteId) {
        fs.collection("restaurantes").document(restauranteId).collection("productos").get()
                .addOnSuccessListener(snap -> {
                    List<Producto> lista = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) lista.add(aProducto(restauranteId, d));
                    AppDatabase.io.execute(() -> db.runInTransaction(() -> {
                        db.productoDao().borrarDeRestaurante(restauranteId);
                        db.productoDao().guardarTodos(lista);
                    }));
                });
    }

    // ---- Conversión de documentos de Firestore a entidades de Room ----
    private static Restaurante aRestaurante(DocumentSnapshot d) {
        Restaurante r = new Restaurante();
        r.id = d.getId();
        r.duenoUid = d.getString("duenoUid");
        r.nombre = d.getString("nombre");
        r.descripcion = d.getString("descripcion");
        r.categoria = d.getString("categoria");
        r.tipoComida = d.getString("tipoComida");
        r.precio = d.getString("precio");
        r.direccion = d.getString("direccion");
        r.telefono = d.getString("telefono");
        r.correo = d.getString("correo");
        r.apertura = d.getString("apertura");
        r.cierre = d.getString("cierre");
        r.plan = d.getString("plan");
        r.imagenUrl = d.getString("imagenUrl");
        Long cap = d.getLong("capacidad");
        r.capacidad = cap == null ? 20 : cap.intValue();
        Double rating = d.getDouble("rating");
        r.rating = rating == null ? 0 : rating;
        Long num = d.getLong("numResenas");
        r.numResenas = num == null ? 0 : num.intValue();
        r.latitud = d.getDouble("latitud");
        r.longitud = d.getDouble("longitud");
        r.categorias = lista(d, "categorias");
        r.etiquetas = lista(d, "etiquetas");
        r.menuClaves = lista(d, "menuClaves");
        return r;
    }

    private static Producto aProducto(String restauranteId, DocumentSnapshot d) {
        Producto p = new Producto();
        p.id = d.getId();
        p.restauranteId = restauranteId;
        p.nombre = d.getString("nombre");
        p.descripcion = d.getString("descripcion");
        Double precio = d.getDouble("precio");
        p.precio = precio == null ? 0 : precio;
        p.imagenUrl = d.getString("imagenUrl");
        Boolean disp = d.getBoolean("disponible");
        p.disponible = disp == null || disp;
        return p;
    }

    // Lee un arreglo de Firestore y lo convierte al texto "a|b|c" de Room
    private static String lista(DocumentSnapshot d, String campo) {
        List<String> res = new ArrayList<>();
        Object o = d.get(campo);
        if (o instanceof List) {
            for (Object x : (List<?>) o) if (x instanceof String) res.add((String) x);
        }
        return Lista.unir(res);
    }
}