package edu.sv.ues.dam235.sabortours.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import edu.sv.ues.dam235.sabortours.data.local.Restaurante;

public class FiltroRestaurantesTest {

    private static Restaurante rest(String id, String nombre, String categoria, String categorias, String menu) {
        Restaurante r = new Restaurante();
        r.id = id;
        r.nombre = nombre;
        r.categoria = categoria;
        r.categorias = categorias;
        r.menuClaves = menu;
        return r;
    }

    private final Restaurante ana = rest("ana", "La Pupusería de Ana", "Otros", "otros", "");
    private final Restaurante cafe = rest("cafe", "Café Santa Tecla", "Cafés", "cafes", "pastel|capuchino");
    private final Restaurante taqueria = rest("taco", "El Rincón", "Otros", "mexicana|otros", "tacos|horchata");
    private final Restaurante viejo = rest("viejo", "Doña Lupe", "Típica", "", "");   // sin lista nueva
    private final List<Restaurante> todos = Arrays.asList(ana, cafe, taqueria, viejo);

    private static void assertIds(List<Restaurante> res, String... ids) {
        assertEquals(ids.length, res.size());
        for (String id : ids) {
            boolean esta = false;
            for (Restaurante r : res) if (r.id.equals(id)) esta = true;
            assertEquals("Falta " + id, true, esta);
        }
    }

    @Test public void pupusasEncuentraLaPupuseria() {
        assertIds(FiltroRestaurantes.aplicar(todos, "Todos", "Pupusas", false), "ana", "viejo");
    }

    @Test public void ignoraTildesYMayusculas() {
        assertIds(FiltroRestaurantes.aplicar(todos, "Todos", "PUPUSERÍA", false), "ana", "viejo");
    }

    @Test public void funcionaMientrasSeEscribe() {
        assertIds(FiltroRestaurantes.aplicar(todos, "Todos", "pupu", false), "ana", "viejo");
    }

    @Test public void buscaEnElMenu() {
        assertIds(FiltroRestaurantes.aplicar(todos, "Todos", "taco", false), "taco");
    }

    @Test public void todasLasPalabrasDebenCoincidir() {
        assertIds(FiltroRestaurantes.aplicar(todos, "Todos", "pupusas ana", false), "ana");
        assertIds(FiltroRestaurantes.aplicar(todos, "Todos", "pupusas sushi", false));
    }

    @Test public void filtraPorCategoriaConVariasCategorias() {
        assertIds(FiltroRestaurantes.aplicar(todos, "Cafés", "", false), "cafe");
        assertIds(FiltroRestaurantes.aplicar(todos, "Mexicana", "", false), "taco");
    }

    @Test public void registroViejoUsaSuCategoriaUnica() {
        assertIds(FiltroRestaurantes.aplicar(todos, "Típica", "", false), "viejo");
    }

    @Test public void sinTextoNiCategoriaDevuelveTodos() {
        assertEquals(4, FiltroRestaurantes.aplicar(todos, "Todos", "", false).size());
    }
}