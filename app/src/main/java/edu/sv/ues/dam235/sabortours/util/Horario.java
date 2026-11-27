package edu.sv.ues.dam235.sabortours.util;

import java.util.Calendar;
import java.util.TimeZone;

public class Horario {

    /** ¿Está abierto ahora (hora de El Salvador)? Si cierra de madrugada, cuenta como día siguiente. */
    public static boolean abiertoAhora(String apertura, String cierre) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("America/El_Salvador"));
        int ahora = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        int a = aMin(apertura);
        int cl = aMin(cierre);
        if (cl <= a) cl += 24 * 60;
        return (ahora >= a && ahora < cl) || (ahora + 1440 >= a && ahora + 1440 < cl);
    }

    public static int aMin(String hhmm) {
        if (hhmm == null || !hhmm.contains(":")) return 0;
        String[] p = hhmm.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }
}