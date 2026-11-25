package edu.sv.ues.dam235.sabortours.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "productos")
public class Producto {
    @PrimaryKey @NonNull
    public String id = "";
    public String restauranteId;
    public String nombre;
    public String descripcion;
    public double precio;
    public String imagenUrl;
    public boolean disponible;
}