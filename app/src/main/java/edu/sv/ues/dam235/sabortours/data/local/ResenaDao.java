package edu.sv.ues.dam235.sabortours.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ResenaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void guardar(Resena resena);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void guardarTodas(List<Resena> lista);

    @Query("DELETE FROM resenas WHERE restauranteId = :rid")
    void borrarDeRestaurante(String rid);

    @Query("SELECT * FROM resenas WHERE restauranteId = :rid ORDER BY fechaMillis DESC")
    LiveData<List<Resena>> deRestaurante(String rid);

    @Query("SELECT * FROM resenas WHERE restauranteId = :rid AND usuarioId = :uid LIMIT 1")
    LiveData<Resena> deUsuario(String rid, String uid);
}