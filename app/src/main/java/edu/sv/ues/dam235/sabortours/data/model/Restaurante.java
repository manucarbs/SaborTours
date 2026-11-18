package edu.sv.ues.dam235.sabortours.data.model;

public class Restaurante {
    private int id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private String precio;
    private double rating;
    private String imagen_url;

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public String getPrecio() { return precio; }
    public double getRating() { return rating; }
    public String getImagen_url() { return imagen_url; }
}