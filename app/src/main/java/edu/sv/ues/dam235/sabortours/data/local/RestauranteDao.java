package edu.sv.ues.dam235.sabortours.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface RestauranteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void guardarTodos(List<Restaurante> lista);

    @Query("DELETE FROM restaurantes")
    void borrarTodos();

    @Query("SELECT * FROM restaurantes ORDER BY nombre")
    LiveData<List<Restaurante>> todos();

    @Query("SELECT * FROM restaurantes WHERE id = :id LIMIT 1")
    LiveData<Restaurante> porId(String id);
}