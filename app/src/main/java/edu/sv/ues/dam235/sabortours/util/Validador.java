package edu.sv.ues.dam235.sabortours.util;

import android.util.Patterns;

public class Validador {
    public static boolean correoValido(String c) {
        return c != null && Patterns.EMAIL_ADDRESS.matcher(c).matches();
    }
    public static boolean passwordValida(String p) {
        return p != null && p.length() >= 8;
    }
    public static boolean nombreValido(String n) {
        return n != null && n.trim().length() >= 3;
    }
}