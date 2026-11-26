package edu.sv.ues.dam235.sabortours.data.repository;

import android.content.Context;
import android.widget.Toast;
import androidx.lifecycle.LiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.sv.ues.dam235.sabortours.data.local.AppDatabase;
import edu.sv.ues.dam235.sabortours.data.local.Producto;
import edu.sv.ues.dam235.sabortours.data.local.Restaurante;

public class NegocioRepository {

    private final Context app;
    private final AppDatabase db;
    private final FirebaseFirestore fs = FirebaseFirestore.getInstance();

    public NegocioRepository(Context context) {
        this.app = context.getApplicationContext();
        this.db = AppDatabase.get(app);
    }

    // El id del negocio es el uid de su dueño
    private static String uid() { return FirebaseAuth.getInstance().getUid(); }

    // ---- Lectura: siempre desde Room ----
    public LiveData<Restaurante> miNegocio() { return db.restauranteDao().porId(uid()); }
    public LiveData<List<Producto>> misProductos() { return db.productoDao().todosDelNegocio(uid()); }

    // ---- Productos ----
    public void guardarProducto(Producto p) {
        String uid = uid();
        CollectionReference col = fs.collection("restaurantes").document(uid).collection("productos");
        // Si es nuevo, Firestore genera el id aquí mismo, sin necesidad de internet
        DocumentReference ref = p.id.isEmpty() ? col.document() : col.document(p.id);
        p.id = ref.getId();
        p.restauranteId = uid;

        AppDatabase.io.execute(() -> db.productoDao().guardar(p));        // 1. Room, al instante

        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", p.nombre);
        datos.put("descripcion", p.descripcion == null ? "" : p.descripcion);
        datos.put("precio", p.precio);
        datos.put("disponible", p.disponible);
        // merge: no borra campos que este código aún no maneja (por ejemplo, la foto)
        ref.set(datos, SetOptions.merge()).addOnFailureListener(this::falloNube);   // 2. Firestore
    }

    public void eliminarProducto(Producto p) {
        AppDatabase.io.execute(() -> db.productoDao().eliminar(p.id));
        fs.collection("restaurantes").document(uid()).collection("productos").document(p.id)
                .delete().addOnFailureListener(this::falloNube);
    }

    // ---- Datos del negocio ----
    public void guardarNegocio(Restaurante r) {
        AppDatabase.io.execute(() -> db.restauranteDao().guardar(r));

        // Solo los campos que el dueño puede cambiar; rating y duenoUid no se envían
        Map<String, Object> m = new HashMap<>();
        m.put("nombre", r.nombre);
        m.put("descripcion", r.descripcion == null ? "" : r.descripcion);
        m.put("categoria", r.categoria);
        m.put("tipoComida", r.tipoComida == null ? "" : r.tipoComida);
        m.put("precio", r.precio);
        m.put("direccion", r.direccion == null ? "" : r.direccion);
        m.put("telefono", r.telefono == null ? "" : r.telefono);
        m.put("correo", r.correo == null ? "" : r.correo);
        m.put("apertura", r.apertura);
        m.put("cierre", r.cierre);
        m.put("capacidad", r.capacidad);
        m.put("plan", r.plan);
        if (r.latitud != null && r.longitud != null) {
            m.put("latitud", r.latitud);
            m.put("longitud", r.longitud);
        }
        fs.collection("restaurantes").document(uid()).update(m).addOnFailureListener(this::falloNube);
    }

    // Si Firestore rechaza el cambio, se vuelve a bajar la versión del servidor y se avisa
    private void falloNube(Exception e) {
        Toast.makeText(app, "No se pudo guardar en la nube; se restauró la versión del servidor", Toast.LENGTH_LONG).show();
        RestauranteRepository repo = new RestauranteRepository(app);
        repo.sincronizarRestaurantes();
        repo.sincronizarProductos(uid());
    }
}