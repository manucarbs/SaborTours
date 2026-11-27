package edu.sv.ues.dam235.sabortours.ui;

import android.view.LayoutInflater;
import android.view.View;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.util.Categorias;

/** Llena un grupo de chips con "Todos" y las categorías del catálogo. */
public class ChipsCategorias {

    public static void llenar(ChipGroup grupo) {
        LayoutInflater inf = LayoutInflater.from(grupo.getContext());
        grupo.removeAllViews();
        agregar(inf, grupo, "Todos").setChecked(true);
        for (Categorias.Categoria c : Categorias.TODAS) agregar(inf, grupo, c.nombre);
    }

    private static Chip agregar(LayoutInflater inf, ChipGroup grupo, String texto) {
        Chip chip = (Chip) inf.inflate(R.layout.item_chip_categoria, grupo, false);
        chip.setId(View.generateViewId());
        chip.setText(texto);
        grupo.addView(chip);
        return chip;
    }
}