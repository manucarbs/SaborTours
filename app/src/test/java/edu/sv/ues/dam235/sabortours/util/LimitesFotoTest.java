package edu.sv.ues.dam235.sabortours.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LimitesFotoTest {

    @Test
    public void unaFotoDeTamanoMaximoCabeEnLasReglas() {
        assertTrue(LimitesFoto.largoComoTexto(LimitesFoto.BYTES_MAX) <= LimitesFoto.TEXTO_MAX_REGLAS);
    }

    @Test
    public void lasReglasQuedanPorDebajoDelLimiteDeFirestore() {
        assertTrue(LimitesFoto.TEXTO_MAX_REGLAS < 1_048_487);   // máx. de un campo en Firestore
    }

    @Test
    public void base64CuentaBien() {
        assertEquals(LimitesFoto.PREFIJO.length() + 4, LimitesFoto.largoComoTexto(3));
        assertEquals(LimitesFoto.PREFIJO.length() + 8, LimitesFoto.largoComoTexto(4));
    }
}