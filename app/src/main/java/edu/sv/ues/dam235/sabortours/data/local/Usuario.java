package edu.sv.ues.dam235.sabortours.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuarios")
public class Usuario {
    @PrimaryKey @NonNull
    public String id = "";   // uid de Firebase
    public String nombre;
    public String correo;
    public String telefono;
    public String rol;       // "cliente" o "comerciante"
}
