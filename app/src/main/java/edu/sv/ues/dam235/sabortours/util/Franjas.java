package edu.sv.ues.dam235.sabortours.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Franjas {

    private static final int PASO = 30;   // minutos entre una franja y la siguiente

    public static List<String> generar(String apertura, String cierre, String fechaIso) {
        List<String> lista = new ArrayList<>();
        int a = Horario.aMin(apertura);
        int c = Horario.aMin(cierre);
        if (c <= a) c += 24 * 60;                 // cierra de madrugada
        int ultima = Math.min(c - 60, 24 * 60 - 1);   // una hora antes del cierre, sin pasar de medianoche

        int minimo = 0;
        if (fechaIso.equals(Formatos.hoyIso())) minimo = ahoraEnMinutos() + 1;

        for (int m = a; m <= ultima; m += PASO) {
            if (m >= minimo) lista.add(String.format(Locale.US, "%02d:%02d", m / 60, m % 60));
        }
        return lista;
    }

    private static int ahoraEnMinutos() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("America/El_Salvador"));
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
    }
}