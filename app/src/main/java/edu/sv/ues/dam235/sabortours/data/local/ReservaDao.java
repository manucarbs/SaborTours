package edu.sv.ues.dam235.sabortours.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ReservaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void guardar(Reserva reserva);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void guardarTodas(List<Reserva> lista);

    @Query("DELETE FROM reservas WHERE usuarioId = :uid")
    void borrarDeCliente(String uid);

    @Query("DELETE FROM reservas WHERE restauranteId = :rid")
    void borrarDeNegocio(String rid);

    @Query("UPDATE reservas SET estado = :estado WHERE id = :id")
    void actualizarEstado(String id, String estado);

    // Cliente
    @Query("SELECT * FROM reservas WHERE usuarioId = :uid AND estado IN ('pendiente','aceptada') AND fecha >= :hoy ORDER BY fecha, hora")
    LiveData<List<Reserva>> proximas(String uid, String hoy);

    @Query("SELECT * FROM reservas WHERE usuarioId = :uid AND (estado NOT IN ('pendiente','aceptada') OR fecha < :hoy) ORDER BY fecha DESC, hora DESC")
    LiveData<List<Reserva>> historial(String uid, String hoy);

    // Dueño del restaurante: reservas de hoy en adelante, de todos los estados
    @Query("SELECT * FROM reservas WHERE restauranteId = :rid AND fecha >= :hoy ORDER BY fecha, hora")
    LiveData<List<Reserva>> proximasDeNegocio(String rid, String hoy);
}