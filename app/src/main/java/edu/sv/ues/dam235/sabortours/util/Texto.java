package edu.sv.ues.dam235.sabortours.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Normalización de texto para el buscador: sin tildes, en minúsculas y con raíz aproximada. */
public class Texto {

    private static final Set<String> RELLENO = new HashSet<>(Arrays.asList(
            "de", "del", "la", "las", "el", "los", "y", "e", "en", "con", "un", "una", "para", "por"));

    // De más largo a más corto: primero se intenta quitar "eria" y, si no, "a"
    private static final String[] SUFIJOS = {
            "erias", "eria", "eros", "eras", "ero", "era", "ones", "es", "as", "os", "on", "s", "a", "o", "e"};

    /** "Pupusería de Ana" -> "pupuseria de ana" */
    public static String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    /** Palabras significativas, sin "de", "la"... */
    public static List<String> palabras(String s) {
        List<String> res = new ArrayList<>();
        for (String p : normalizar(s).split(" ")) {
            if (!p.isEmpty() && !RELLENO.contains(p)) res.add(p);
        }
        return res;
    }

    /** Raíz aproximada: "pupusas" y "pupuseria" -> "pupus". Siempre deja al menos 4 letras. */
    public static String raiz(String palabra) {
        for (String suf : SUFIJOS) {
            if (palabra.endsWith(suf) && palabra.length() - suf.length() >= 4) {
                return palabra.substring(0, palabra.length() - suf.length());
            }
        }
        return palabra;
    }

    public static List<String> raices(String s) {
        List<String> res = new ArrayList<>();
        for (String p : palabras(s)) res.add(raiz(p));
        return res;
    }

    /** ¿La raíz buscada coincide con la raíz de un texto? Sirve también mientras se escribe ("pupu"). */
    public static boolean coincide(String buscada, String candidata) {
        if (candidata.startsWith(buscada)) return true;
        return candidata.length() >= 4 && buscada.startsWith(candidata);
    }

    /** Palabras distintas (de 3 letras o más) de varios textos, para guardar las claves del menú. */
    public static List<String> clavesDe(List<String> textos, int max) {
        Set<String> claves = new LinkedHashSet<>();
        for (String t : textos) {
            for (String p : palabras(t)) {
                if (p.length() >= 3 && claves.size() < max) claves.add(p);
            }
        }
        return new ArrayList<>(claves);
    }
}