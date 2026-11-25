package edu.sv.ues.dam235.sabortours.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ReservaDao {

    @Insert
    long insertar(Reserva reserva);

    @Query("SELECT * FROM reservas WHERE estado IN ('pendiente','aceptada') AND fecha >= :hoy ORDER BY fecha, hora")
    LiveData<List<Reserva>> proximas(String hoy);

    @Query("SELECT * FROM reservas WHERE estado NOT IN ('pendiente','aceptada') OR fecha < :hoy ORDER BY fecha DESC, hora DESC")
    LiveData<List<Reserva>> historial(String hoy);

    @Query("UPDATE reservas SET estado = 'cancelada' WHERE id = :id")
    void cancelar(int id);
}