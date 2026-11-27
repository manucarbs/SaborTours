package edu.sv.ues.dam235.sabortours.ui.explorar;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.local.Restaurante;
import edu.sv.ues.dam235.sabortours.data.repository.RestauranteRepository;
import edu.sv.ues.dam235.sabortours.ui.home.RestauranteAdapter;
import edu.sv.ues.dam235.sabortours.util.FiltroRestaurantes;
import java.util.HashSet;
import java.util.Set;
import edu.sv.ues.dam235.sabortours.data.repository.FavoritoRepository;

public class ExplorarFragment extends Fragment {

    private List<Restaurante> todos = new ArrayList<>();
    private RestauranteAdapter adapter;
    private TextView tvVacio;

    // Estado actual de los filtros
    private String categoria = "Todos";
    private String texto = "";
    private boolean ordenarPorRating = false;
    private boolean soloFavoritos = false;
    private Set<String> idsFav = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explorar, container, false);

        RecyclerView rv = view.findViewById(R.id.rvExplorar);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RestauranteAdapter();
        rv.setAdapter(adapter);
        tvVacio = view.findViewById(R.id.tvVacio);

        new RestauranteRepository(requireContext()).restaurantes()
                .observe(getViewLifecycleOwner(), lista -> {
                    todos = lista;
                    aplicar();
                });

        EditText etBuscar = view.findViewById(R.id.etBuscar);
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                texto = s.toString().trim();
                aplicar();
            }
        });

        ChipGroup grupo = view.findViewById(R.id.chipGroupCategorias);
        grupo.setOnCheckedStateChangeListener((g, ids) -> {
            if (ids.isEmpty()) return;
            Chip chip = g.findViewById(ids.get(0));
            categoria = chip.getText().toString();
            aplicar();
        });

        Chip chipRating = view.findViewById(R.id.chipRating);
        chipRating.setOnCheckedChangeListener((b, marcado) -> {
            ordenarPorRating = marcado;
            aplicar();
        });

        FavoritoRepository favRepo = new FavoritoRepository(requireContext());
        favRepo.sincronizar();
        favRepo.idsFavoritos().observe(getViewLifecycleOwner(), ids -> {
            idsFav = new HashSet<>(ids);
            aplicar();
        });
        Chip chipFavoritos = view.findViewById(R.id.chipFavoritos);
        chipFavoritos.setOnCheckedChangeListener((b, marcado) -> {
            soloFavoritos = marcado;
            aplicar();
        });

        return view;
    }

    private void aplicar() {
        List<Restaurante> base = todos;
        if (soloFavoritos) {
            base = new ArrayList<>();
            for (Restaurante r : todos) if (idsFav.contains(r.id)) base.add(r);
        }
        List<Restaurante> res = FiltroRestaurantes.aplicar(base, categoria, texto, ordenarPorRating);
        adapter.setLista(res);
        tvVacio.setText(soloFavoritos && idsFav.isEmpty()
                ? "Aún no tienes favoritos. Toca el corazón en un restaurante."
                : todos.isEmpty() ? "Aún no hay restaurantes para mostrar" : "No encontramos resultados");
        tvVacio.setVisibility(res.isEmpty() ? View.VISIBLE : View.GONE);
    }
}