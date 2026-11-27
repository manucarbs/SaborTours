package edu.sv.ues.dam235.sabortours.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import edu.sv.ues.dam235.sabortours.data.local.Restaurante;

public class FiltroRestaurantes {

    /** categoria: "Todos" o el nombre de una categoría del catálogo. */
    public static List<Restaurante> aplicar(List<Restaurante> todos, String categoria, String texto, boolean porRating) {
        String idCategoria = idDeCategoria(categoria);      // null = todas
        List<String> buscadas = Texto.raices(texto);
        List<Restaurante> res = new ArrayList<>();

        for (Restaurante r : todos) {
            if (idCategoria != null && !idsDe(r).contains(idCategoria)) continue;
            if (!coincideTexto(r, buscadas)) continue;
            res.add(r);
        }

        if (porRating) Collections.sort(res, (a, b) -> Double.compare(b.rating, a.rating));
        else Collections.sort(res, (a, b) -> Double.compare(distancia(a), distancia(b)));
        return res;
    }

    private static String idDeCategoria(String categoria) {
        if (categoria == null || categoria.equals("Todos")) return null;
        Categorias.Categoria c = Categorias.buscar(categoria);
        return c == null ? categoria : c.id;
    }

    /** Ids de categoría del negocio. Si es un registro viejo (solo "categoria"), se usa ese valor. */
    static List<String> idsDe(Restaurante r) {
        List<String> ids = new ArrayList<>();
        for (String v : Lista.separar(r.categorias)) {
            Categorias.Categoria c = Categorias.buscar(v);
            if (c != null) ids.add(c.id);
        }
        if (ids.isEmpty()) {
            Categorias.Categoria c = Categorias.buscar(r.categoria);
            if (c != null) ids.add(c.id);
        }
        return ids;
    }

    // Todas las palabras buscadas deben aparecer (por raíz) en algún campo del restaurante
    private static boolean coincideTexto(Restaurante r, List<String> buscadas) {
        if (buscadas.isEmpty()) return true;
        List<String> candidatas = raicesDe(r);
        for (String q : buscadas) {
            boolean hallada = false;
            for (String c : candidatas) {
                if (Texto.coincide(q, c)) { hallada = true; break; }
            }
            if (!hallada) return false;
        }
        return true;
    }

    private static List<String> raicesDe(Restaurante r) {
        StringBuilder sb = new StringBuilder();
        sb.append(r.nombre).append(' ').append(r.tipoComida).append(' ');
        for (String id : idsDe(r)) {
            Categorias.Categoria c = Categorias.buscar(id);
            sb.append(c.nombre).append(' ');
            for (String p : c.palabras) sb.append(p).append(' ');
        }
        for (String e : Lista.separar(r.etiquetas)) sb.append(e).append(' ');
        for (String m : Lista.separar(r.menuClaves)) sb.append(m).append(' ');
        return Texto.raices(sb.toString());
    }

    private static double distancia(Restaurante r) {
        Double d = Ubicacion.distanciaKm(r.latitud, r.longitud);
        return d == null ? Double.MAX_VALUE : d;
    }
}