package edu.sv.ues.dam235.sabortours.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "restaurantes")
public class Restaurante {
    @PrimaryKey @NonNull
    public String id = "";          // uid del dueño
    public String duenoUid;
    public String nombre;
    public String descripcion;
    public String categoria;        // Típica, Hamburguesas, Pizza, Cafés
    public String tipoComida;       // "Comida salvadoreña"
    public String precio;           // $, $$, $$$
    public String direccion;
    public String telefono;
    public String correo;
    public String apertura;         // "16:00"
    public String cierre;           // "21:00"
    public String plan;
    public String imagenUrl;
    public int capacidad;
    public double rating;
    public int numResenas;
    public Double latitud;          // pueden ser nulos
    public Double longitud;
}