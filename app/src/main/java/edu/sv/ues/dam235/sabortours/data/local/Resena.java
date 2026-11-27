package edu.sv.ues.dam235.sabortours.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "resenas", primaryKeys = {"restauranteId", "usuarioId"})
public class Resena {
    @NonNull public String restauranteId = "";
    @NonNull public String usuarioId = "";     // uid del autor (también es el id del documento)
    public String autorNombre;
    public int calificacion;                   // 1 a 5
    public String comentario;
    public long fechaMillis;
}