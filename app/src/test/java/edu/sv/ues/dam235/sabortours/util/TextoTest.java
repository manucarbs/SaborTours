package edu.sv.ues.dam235.sabortours.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;

public class TextoTest {

    @Test public void normalizaTildesYSimbolos() {
        assertEquals("pupuseria de ana", Texto.normalizar("  Pupusería de Ana! "));
    }

    @Test public void palabrasIgnoraRelleno() {
        assertEquals(Arrays.asList("pupuseria", "ana"), Texto.palabras("La Pupusería de Ana"));
    }

    @Test public void pupusasYPupuseriaComparteRaiz() {
        assertEquals("pupus", Texto.raiz("pupusas"));
        assertEquals("pupus", Texto.raiz("pupuseria"));
    }

    @Test public void raicesCortasNoSeDestruyen() {
        assertEquals("cafe", Texto.raiz("cafes"));
        assertEquals("pizz", Texto.raiz("pizzeria"));
    }

    @Test public void coincideSinFalsosPositivos() {
        assertTrue(Texto.coincide("pup", "pupus"));
        assertTrue(Texto.coincide("cafeteri", "cafe"));
        assertFalse(Texto.coincide("pizz", "pupus"));
    }

    @Test public void clavesDelMenu() {
        assertEquals(Arrays.asList("pupusas", "revueltas", "cafe"),
                Texto.clavesDe(Arrays.asList("Pupusas revueltas", "Café", "de"), 10));
    }
}