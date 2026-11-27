package edu.sv.ues.dam235.sabortours.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Formatos {

    /** "2026-11-25" -> "25 de noviembre de 2026" */
    public static String fechaBonita(String iso) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso);
            return new SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-SV")).format(d);
        } catch (ParseException e) {
            return iso;
        }
    }

    /** "19:00" -> "7:00 PM" */
    public static String hora12(String hhmm) {
        try {
            Date d = new SimpleDateFormat("HH:mm", Locale.US).parse(hhmm);
            return new SimpleDateFormat("h:mm a", Locale.US).format(d);
        } catch (ParseException e) {
            return hhmm;
        }
    }

    /** Fecha de hoy en El Salvador, como "2026-11-20" */
    public static String hoyIso() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("America/El_Salvador"));
        return f.format(new Date());
    }
}