package edu.sv.ues.dam235.sabortours.util;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import org.junit.Test;

public class CalificacionTest {

    @Test
    public void primeraResena() {
        Calificacion.Resumen r = Calificacion.conNueva(0, 0, 5);
        assertEquals(5.0, r.rating, 1e-9);
        assertEquals(1, r.total);
    }

    @Test
    public void segundaResena() {
        Calificacion.Resumen r = Calificacion.conNueva(5.0, 1, 3);
        assertEquals(4.0, r.rating, 1e-9);
        assertEquals(2, r.total);
    }

    @Test
    public void editarResena() {
        Calificacion.Resumen r = Calificacion.conEditada(4.0, 2, 5, 1);   // 5 y 3 -> 1 y 3
        assertEquals(2.0, r.rating, 1e-9);
        assertEquals(2, r.total);
    }

    @Test(expected = IllegalArgumentException.class)
    public void editarSinResenasFalla() {
        Calificacion.conEditada(0, 0, 3, 4);
    }

    @Test
    public void noHayDerivaDespuesDeMuchasOperaciones() {
        Random azar = new Random(235);
        int[] votos = new int[200];
        double rating = 0;
        int total = 0;

        for (int i = 0; i < votos.length; i++) {            // 200 reseñas nuevas
            votos[i] = 1 + azar.nextInt(5);
            Calificacion.Resumen r = Calificacion.conNueva(rating, total, votos[i]);
            rating = r.rating;
            total = r.total;
        }
        for (int k = 0; k < 1000; k++) {                     // 1000 ediciones al azar
            int i = azar.nextInt(votos.length);
            int nuevo = 1 + azar.nextInt(5);
            Calificacion.Resumen r = Calificacion.conEditada(rating, total, votos[i], nuevo);
            votos[i] = nuevo;
            rating = r.rating;
        }

        long sumaReal = 0;
        for (int v : votos) sumaReal += v;
        assertEquals((double) sumaReal / votos.length, rating, 1e-9);
        assertEquals(sumaReal, Calificacion.suma(rating, total));
    }
}