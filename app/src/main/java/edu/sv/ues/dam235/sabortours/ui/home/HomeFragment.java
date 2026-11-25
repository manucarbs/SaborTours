package edu.sv.ues.dam235.sabortours.ui.home;

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
import edu.sv.ues.dam235.sabortours.util.FiltroRestaurantes;

public class HomeFragment extends Fragment {

    private List<Restaurante> todos = new ArrayList<>();
    private RestauranteAdapter adapter;
    private TextView tvVacio;
    private String categoria = "Todos";
    private String texto = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView rv = view.findViewById(R.id.rvRestaurantes);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setNestedScrollingEnabled(false);   // el ScrollView de afuera es quien desplaza
        adapter = new RestauranteAdapter();
        rv.setAdapter(adapter);
        tvVacio = view.findViewById(R.id.tvVacioHome);

        // Cada vez que Room cambia (por una sincronización), la lista se actualiza sola
        new RestauranteRepository(requireContext()).restaurantes()
                .observe(getViewLifecycleOwner(), lista -> {
                    todos = lista;
                    aplicar();
                });

        EditText etBuscar = view.findViewById(R.id.etBuscarHome);
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                texto = s.toString().trim();
                aplicar();
            }
        });

        ChipGroup grupo = view.findViewById(R.id.chipGroupHome);
        grupo.setOnCheckedStateChangeListener((g, ids) -> {
            if (ids.isEmpty()) return;
            Chip chip = g.findViewById(ids.get(0));
            categoria = chip.getText().toString();
            aplicar();
        });

        return view;
    }

    private void aplicar() {
        List<Restaurante> res = FiltroRestaurantes.aplicar(todos, categoria, texto, false);
        adapter.setLista(res);
        tvVacio.setText(todos.isEmpty() ? "Aún no hay restaurantes para mostrar" : "No encontramos resultados");
        tvVacio.setVisibility(res.isEmpty() ? View.VISIBLE : View.GONE);
    }
}