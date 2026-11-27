package edu.sv.ues.dam235.sabortours.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface FavoritoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void guardar(Favorito favorito);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void guardarTodos(List<Favorito> lista);

    @Query("DELETE FROM favoritos WHERE usuarioId = :uid AND restauranteId = :rid")
    void borrar(String uid, String rid);

    @Query("DELETE FROM favoritos WHERE usuarioId = :uid")
    void borrarDeUsuario(String uid);

    // 1 si es favorito, 0 si no
    @Query("SELECT COUNT(*) FROM favoritos WHERE usuarioId = :uid AND restauranteId = :rid")
    LiveData<Integer> contar(String uid, String rid);

    @Query("SELECT restauranteId FROM favoritos WHERE usuarioId = :uid")
    LiveData<List<String>> ids(String uid);
}