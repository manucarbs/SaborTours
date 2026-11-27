package edu.sv.ues.dam235.sabortours.util;

/** Límites de las fotos guardadas como texto en Firestore. */
public final class LimitesFoto {
    private LimitesFoto() { }

    public static final int LADO_MAX = 800;               // px del lado más largo
    public static final int BYTES_MAX = 120_000;          // peso máximo del JPEG
    public static final int TEXTO_MAX_REGLAS = 200_000;   // debe coincidir con imagenOk() en las reglas
    public static final String PREFIJO = "data:image/jpeg;base64,";

    // Base64 convierte cada 3 bytes en 4 caracteres (con relleno)
    public static int largoComoTexto(int bytes) {
        return PREFIJO.length() + 4 * ((bytes + 2) / 3);
    }
}