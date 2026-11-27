package edu.sv.ues.dam235.sabortours.util;

public class Ubicacion {
    // Ubicación por defecto (Santa Tecla); el GPS la reemplazará más adelante
    public static double lat = 13.6769;
    public static double lng = -89.2797;

    /** Distancia en km desde la ubicación actual (fórmula de Haversine). Nulo si falta el punto. */
    public static Double distanciaKm(Double lat2, Double lng2) {
        if (lat2 == null || lng2 == null) return null;
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat);
        double dLon = Math.toRadians(lng2 - lng);
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLon / 2), 2);
        double km = R * 2 * Math.asin(Math.sqrt(a));
        return Math.round(km * 10) / 10.0;
    }
}