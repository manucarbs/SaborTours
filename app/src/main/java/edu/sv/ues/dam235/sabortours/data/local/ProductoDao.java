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

    @Query("DELETE FROM productos WHERE restauranteId = :restauranteId")
    void borrarDeRestaurante(String restauranteId);

    @Query("SELECT * FROM productos WHERE restauranteId = :restauranteId AND disponible = 1 ORDER BY nombre")
    LiveData<List<Producto>> deRestaurante(String restauranteId);
}