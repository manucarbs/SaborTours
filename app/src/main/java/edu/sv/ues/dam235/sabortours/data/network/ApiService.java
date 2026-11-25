package edu.sv.ues.dam235.sabortours.data.network;

import java.util.List;
import edu.sv.ues.dam235.sabortours.data.model.LoginRequest;
import edu.sv.ues.dam235.sabortours.data.model.LoginResponse;
import edu.sv.ues.dam235.sabortours.data.model.Restaurante;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    @GET("api/restaurantes")
    Call<List<Restaurante>> getRestaurantes();
}