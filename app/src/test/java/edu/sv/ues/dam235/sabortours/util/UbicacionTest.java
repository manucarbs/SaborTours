package edu.sv.ues.dam235.sabortours.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class UbicacionTest {

    @Before
    public void restaurarSantaTecla() {
        Ubicacion.lat = 13.6769;
        Ubicacion.lng = -89.2797;
    }

    @Test
    public void mismoPuntoEsCero() {
        assertEquals(0.0, Ubicacion.distanciaKm(13.6769, -89.2797), 1e-9);
    }

    @Test
    public void santaTeclaASanSalvador() {
        assertEquals(6.9, Ubicacion.distanciaKm(13.6929, -89.2182), 0.3);
    }

    @Test
    public void sinCoordenadasDevuelveNulo() {
        assertNull(Ubicacion.distanciaKm(null, -89.0));
        assertNull(Ubicacion.distanciaKm(13.0, null));
    }

    @Test
    public void lasDistanciasSeCalculanDesdeLaUbicacionDelGps() {
        Ubicacion.lat = 13.6929;
        Ubicacion.lng = -89.2182;
        assertEquals(0.0, Ubicacion.distanciaKm(13.6929, -89.2182), 1e-9);
    }
}