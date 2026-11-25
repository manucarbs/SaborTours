package edu.sv.ues.dam235.sabortours.data.model;
public class LoginResponse {
    private String token;
    private Usuario usuario;
    public String getToken() { return token; }
    public Usuario getUsuario() { return usuario; }
    public static class Usuario {
        private int id;
        private String nombre;
        private String email;
        private String rol;
        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getEmail() { return email; }
        public String getRol() { return rol; }
    }
}