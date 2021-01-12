package com.melobarros.autocasher.services;

import com.melobarros.autocasher.model.Abastecimento;
import com.melobarros.autocasher.model.Gasto;
import com.melobarros.autocasher.model.Lembrete;
import com.melobarros.autocasher.model.Manutencao;
import com.melobarros.autocasher.model.Registro;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface autocasherAPI {

    public static final String BASE_URL = "https://autocasher.herokuapp.com/api/";

    @GET("registros")
    Call<List<Registro>> getRegistros();

    @GET("registro/{id}")
    Call<Registro> getRegistroById(@Path("id") int id);

    @POST("registro")
    Call<String> insertRegistro(@Body Registro registro);

    @PUT("registro")
    Call<String> updateRegistro(@Body Registro registro);

    @DELETE("registro")
    Call<String> deleteRegistro(@Body Registro registro);

    //------------------------------------------------------

    @GET("registros/gastos")
    Call<List<Gasto>> getGastos();

    @GET("registro/gasto/{id}")
    Call<Gasto> getGastoById(@Path("id") int id);

    @POST("registro/gasto")
    Call<String> insertGasto(@Body Gasto gasto);

    @PUT("registro/gasto")
    Call<String> updateGasto(@Body Gasto gasto);

    @DELETE("registro/gasto")
    Call<String> deleteGasto(@Body Gasto gasto);


    //------------------------------------------------------

    @GET("registros/abastecimentos")
    Call<List<Abastecimento>> getAbastecimentos();

    @GET("registro/abastecimento/{id}")
    Call<Abastecimento> getAbastecimentoById(@Path("id") int id);

    @POST("registro/abastecimento")
    Call<String> insertAbastecimento(@Body Abastecimento abastecimento);

    @PUT("registro/abastecimento")
    Call<String> updateAbastecimento(@Body Abastecimento abastecimento);

    @DELETE("registro/abastecimento")
    Call<String> deleteAbastecimento(@Body Abastecimento abastecimento);

    //------------------------------------------------------

    @GET("registros/lembretes")
    Call<List<Lembrete>> getLembretes();

    @GET("registro/lembrete/{id}")
    Call<Lembrete> getLembreteById(@Path("id") int id);

    @POST("registro/lembrete")
    Call<String> insertLembrete(@Body Lembrete lembrete);

    @PUT("registro/lembrete")
    Call<String> updateLembrete(@Body Lembrete lembrete);

    @DELETE("registro/lembrete")
    Call<String> deleteLembrete(@Body Lembrete lembrete);

    //------------------------------------------------------

    @GET("registros/manutencoes")
    Call<List<Manutencao>> getManutencoes();

    @GET("registro/manutencao/{id}")
    Call<Manutencao> getManutencaoById(@Path("id") int id);

    @POST("registro/manutencao")
    Call<String> insertManutencao(@Body Manutencao manutencao);

    @PUT("registro/manutencao")
    Call<String> updateManutencao(@Body Manutencao manutencao);

    @DELETE("registro/manutencao")
    Call<String> deleteManutencao(@Body Manutencao manutencao);

}
