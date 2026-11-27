package edu.sv.ues.dam235.sabortours.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Catálogo único de categorías: lo usan el registro, la configuración y los chips. */
public class Categorias {

    public static final int MAX_POR_NEGOCIO = 3;

    public static class Categoria {
        public final String id;          // estable: es lo que se guarda en Firestore
        public final String nombre;      // lo que ve el usuario
        public final List<String> palabras;   // sinónimos que también encuentra el buscador

        Categoria(String id, String nombre, String... palabras) {
            this.id = id;
            this.nombre = nombre;
            this.palabras = Arrays.asList(palabras);
        }
    }

    public static final List<Categoria> TODAS = Collections.unmodifiableList(Arrays.asList(
            new Categoria("tipica", "Típica", "salvadoreña", "nacional", "tradicional", "antojitos", "pupusas", "tamales", "comedor", "desayunos"),
            new Categoria("hamburguesas", "Hamburguesas", "comida rápida", "hotdog", "papas", "sandwich", "alitas"),
            new Categoria("pizza", "Pizza", "pasta", "italiana", "lasaña", "espagueti"),
            new Categoria("cafes", "Cafés", "cafetería", "café", "bebidas", "desayunos", "repostería"),
            new Categoria("postres", "Postres y helados", "helado", "pasteles", "dulces", "heladería"),
            new Categoria("panaderia", "Panadería", "pan", "pastelería", "repostería"),
            new Categoria("mariscos", "Mariscos", "pescado", "camarones", "ceviche", "marisquería"),
            new Categoria("carnes", "Carnes y parrilla", "asado", "parrillada", "churrasco", "steak"),
            new Categoria("pollo", "Pollo", "pollo frito", "alitas", "rostizado", "broaster"),
            new Categoria("asiatica", "Asiática", "sushi", "china", "japonesa", "tailandesa", "ramen"),
            new Categoria("mexicana", "Mexicana", "tacos", "burritos", "nachos", "quesadillas"),
            new Categoria("saludable", "Saludable", "ensaladas", "vegetariana", "vegana", "jugos", "fit"),
            new Categoria("bar", "Bar", "cocteles", "cerveza", "licores", "tragos", "bebidas"),
            new Categoria("otros", "Otros")
    ));

    /** Busca por id ("tipica") o por nombre ("Típica", incluso sin tilde). Null si no existe. */
    public static Categoria buscar(String valor) {
        if (valor == null) return null;
        String v = Texto.normalizar(valor);
        for (Categoria c : TODAS) {
            if (c.id.equals(v) || Texto.normalizar(c.nombre).equals(v)) return c;
        }
        return null;
    }

    public static List<String> nombres() {
        List<String> res = new ArrayList<>();
        for (Categoria c : TODAS) res.add(c.nombre);
        return res;
    }
}