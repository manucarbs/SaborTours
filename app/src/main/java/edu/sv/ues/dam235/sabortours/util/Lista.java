package edu.sv.ues.dam235.sabortours.util;

import java.util.ArrayList;
import java.util.List;

/** Listas de texto guardadas en una sola columna de Room: "tipica|cafes". */
public class Lista {

    public static String unir(List<String> items) {
        if (items == null || items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String s : items) {
            if (sb.length() > 0) sb.append('|');
            sb.append(s);
        }
        return sb.toString();
    }

    public static List<String> separar(String texto) {
        List<String> res = new ArrayList<>();
        if (texto == null || texto.isEmpty()) return res;
        for (String s : texto.split("\\|")) if (!s.isEmpty()) res.add(s);
        return res;
    }
}