package edu.sv.ues.dam235.sabortours.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.model.Restaurante;
import edu.sv.ues.dam235.sabortours.data.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView rvRestaurantes;
    private RestauranteAdapter adapter;
    private List<Restaurante> listaRestaurantes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvRestaurantes = view.findViewById(R.id.rvRestaurantes);
        rvRestaurantes.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RestauranteAdapter(listaRestaurantes);
        rvRestaurantes.setAdapter(adapter);

        cargarRestaurantes();

        return view;
    }

    private void cargarRestaurantes() {
        RetrofitClient.getApiService().getRestaurantes().enqueue(new Callback<List<Restaurante>>() {
            @Override
            public void onResponse(Call<List<Restaurante>> call, Response<List<Restaurante>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaRestaurantes.clear();
                    listaRestaurantes.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Restaurante>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}