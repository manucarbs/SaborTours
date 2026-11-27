package edu.sv.ues.dam235.sabortours.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reservas")
public class Reserva {
    @PrimaryKey @NonNull
    public String id = "";          // id del documento en Firestore
    public String usuarioId;        // uid del cliente
    public String restauranteId;    // uid del dueño
    public String restauranteNombre;
    public String clienteNombre;
    public String fecha;            // "2026-11-26"
    public String hora;             // "19:00" (24 horas)
    public int personas;
    public String estado;           // pendiente, aceptada, rechazada o cancelada
}