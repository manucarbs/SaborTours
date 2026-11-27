package edu.sv.ues.dam235.sabortours.util;

/** Recalcula el promedio de un restaurante sin leer todas las reseñas. */
public class Calificacion {

    public static class Resumen {
        public final double rating;
        public final int total;
        Resumen(double rating, int total) { this.rating = rating; this.total = total; }
    }

    // La suma de calificaciones es un entero, por eso Math.round la recupera exacta
    public static long suma(double rating, long total) {
        return Math.round(rating * total);
    }

    public static Resumen conNueva(double rating, long total, int nueva) {
        long s = suma(rating, total) + nueva;
        long t = total + 1;
        return new Resumen((double) s / t, (int) t);
    }

    public static Resumen conEditada(double rating, long total, int anterior, int nueva) {
        if (total < 1) throw new IllegalArgumentException("No hay reseñas que editar");
        long s = suma(rating, total) - anterior + nueva;
        return new Resumen((double) s / total, (int) total);
    }
}