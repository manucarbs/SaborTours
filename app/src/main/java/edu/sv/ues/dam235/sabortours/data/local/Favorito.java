package edu.sv.ues.dam235.sabortours.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "favoritos", primaryKeys = {"usuarioId", "restauranteId"})
public class Favorito {
    @NonNull public String usuarioId = "";      // uid del cliente
    @NonNull public String restauranteId = "";  // uid del dueño del restaurante
}