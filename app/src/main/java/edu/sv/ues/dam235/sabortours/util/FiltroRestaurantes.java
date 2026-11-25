package edu.sv.ues.dam235.sabortours.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import edu.sv.ues.dam235.sabortours.data.local.Restaurante;

public class FiltroRestaurantes {

    public static List<Restaurante> aplicar(List<Restaurante> todos, String categoria, String texto, boolean porRating) {
        String buscado = normalizar(texto);
        List<Restaurante> res = new ArrayList<>();

        for (Restaurante r : todos) {
            boolean okCategoria = categoria == null || categoria.equals("Todos")
                    || (r.categoria != null && r.categoria.equalsIgnoreCase(categoria));
            boolean okTexto = buscado.isEmpty()
                    || normalizar(r.nombre).contains(buscado)
                    || normalizar(r.categoria).contains(buscado)
                    || normalizar(r.tipoComida).contains(buscado);
            if (okCategoria && okTexto) res.add(r);
        }

        if (porRating) Collections.sort(res, (a, b) -> Double.compare(b.rating, a.rating));
        else Collections.sort(res, (a, b) -> Double.compare(distancia(a), distancia(b)));
        return res;
    }

    private static double distancia(Restaurante r) {
        Double d = Ubicacion.distanciaKm(r.latitud, r.longitud);
        return d == null ? Double.MAX_VALUE : d;
    }

    // Quita tildes y mayúsculas: "Pupusería" coincide con "pupuseria"
    private static String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}