package edu.sv.ues.dam235.sabortours.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Reserva.class, Usuario.class, Restaurante.class, Producto.class,
        Favorito.class, Resena.class},
        version = 7, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ReservaDao reservaDao();
    public abstract UsuarioDao usuarioDao();
    public abstract RestauranteDao restauranteDao();
    public abstract ProductoDao productoDao();
    public abstract FavoritoDao favoritoDao();
    public abstract ResenaDao resenaDao();

    // Hilo de fondo para escribir en la base de datos
    public static final ExecutorService io = Executors.newSingleThreadExecutor();

    private static volatile AppDatabase instancia;

    public static AppDatabase get(Context context) {
        if (instancia == null) {
            synchronized (AppDatabase.class) {
                if (instancia == null) {
                    instancia = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "sabortours.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instancia;
    }
}