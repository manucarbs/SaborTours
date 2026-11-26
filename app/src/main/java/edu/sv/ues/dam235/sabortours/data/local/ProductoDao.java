package edu.sv.ues.dam235.sabortours.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ProductoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void guardarTodos(List<Producto> lista);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void guardar(Producto producto);

    @Query("DELETE FROM productos WHERE restauranteId = :restauranteId")
    void borrarDeRestaurante(String restauranteId);

    @Query("DELETE FROM productos WHERE id = :id")
    void eliminar(String id);

    // Para los clientes: solo los productos disponibles
    @Query("SELECT * FROM productos WHERE restauranteId = :restauranteId AND disponible = 1 ORDER BY nombre")
    LiveData<List<Producto>> deRestaurante(String restauranteId);

    // Para el dueño: todos, incluidos los que ocultó
    @Query("SELECT * FROM productos WHERE restauranteId = :restauranteId ORDER BY nombre")
    LiveData<List<Producto>> todosDelNegocio(String restauranteId);
}