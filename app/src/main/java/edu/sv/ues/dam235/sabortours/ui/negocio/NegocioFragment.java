package edu.sv.ues.dam235.sabortours.ui.negocio;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.local.Producto;
import edu.sv.ues.dam235.sabortours.data.repository.NegocioRepository;

public class NegocioFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_negocio, container, false);
        NegocioRepository repo = new NegocioRepository(requireContext());

        RecyclerView rv = view.findViewById(R.id.rvProductos);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        ProductoNegocioAdapter adapter = new ProductoNegocioAdapter(new ProductoNegocioAdapter.Acciones() {
            @Override
            public void editar(Producto p) {
                Intent i = new Intent(requireContext(), ProductoFormActivity.class);
                i.putExtra("id", p.id);
                i.putExtra("nombre", p.nombre);
                i.putExtra("descripcion", p.descripcion);
                i.putExtra("precio", p.precio);
                i.putExtra("disponible", p.disponible);
                i.putExtra("imagen", p.imagenUrl);
                startActivity(i);
            }

            @Override
            public void eliminar(Producto p) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Eliminar producto")
                        .setMessage("¿Quieres quitar «" + p.nombre + "» de tu menú?")
                        .setPositiveButton("Eliminar", (d, w) -> repo.eliminarProducto(p))
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });
        rv.setAdapter(adapter);

        View tvVacio = view.findViewById(R.id.tvSinProductos);
        repo.misProductos().observe(getViewLifecycleOwner(), lista -> {
            adapter.setLista(lista);
            tvVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        });

        view.findViewById(R.id.btnPublicarProducto).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ProductoFormActivity.class)));
        return view;
    }
}