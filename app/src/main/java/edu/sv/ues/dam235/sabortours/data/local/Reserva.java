package edu.sv.ues.dam235.sabortours.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reservas")
public class Reserva {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int restauranteId;
    public String restauranteNombre;
    public String fecha;     // "2026-11-25"  (año-mes-día)
    public String hora;      // "19:00"       (24 horas)
    public int personas;
    public String estado;    // "pendiente", "aceptada" o "cancelada"
}