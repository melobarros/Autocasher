package com.melobarros.autocasher.services;

import com.melobarros.autocasher.model.Abastecimento;
import com.melobarros.autocasher.model.Gasto;
import com.melobarros.autocasher.model.Lembrete;
import com.melobarros.autocasher.model.Manutencao;
import com.melobarros.autocasher.model.Registro;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface autocasherAPI {

    public static final String BASE_URL = "https://autocasher.herokuapp.com/api/";

    @GET("registros")
    Call<List<Registro>> getRegistros();

    @GET("registro/{startDate}/{endDate}")
    Call<List<Registro>> getRegistrosBetweenDates(@Path("startDate") String startDate, @Path("endDate") String endDate);

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

    @GET("registro/gasto/{startDate}/{endDate}")
    Call<List<Gasto>> getGastosBetweenDates(@Path("startDate") String startDate, @Path("endDate") String endDate);

    @GET("registro/gasto/{id}")
    Call<Gasto> getGastoById(@Path("id") int id);

    @POST("registro/gasto")
    Call<Gasto> insertGasto(@Body Gasto gasto);

    @PUT("registro/gasto")
    Call<Gasto> updateGasto(@Body Gasto gasto);

    @HTTP(method = "DELETE", path = "registro/gasto", hasBody = true)
    Call<Void> deleteGasto(@Body Gasto gasto);


    //------------------------------------------------------

    @GET("registros/abastecimentos")
    Call<List<Abastecimento>> getAbastecimentos();

    @GET("registro/abastecimento/{startDate}/{endDate}")
    Call<List<Abastecimento>> getAbastecimentosBetweenDates(@Path("startDate") String startDate, @Path("endDate") String endDate);

    @GET("registro/abastecimento/{id}")
    Call<Abastecimento> getAbastecimentoById(@Path("id") int id);

    @POST("registro/abastecimento")
    Call<Abastecimento> insertAbastecimento(@Body Abastecimento abastecimento);

    @PUT("registro/abastecimento")
    Call<Abastecimento> updateAbastecimento(@Body Abastecimento abastecimento);

    @HTTP(method = "DELETE", path = "registro/abastecimento", hasBody = true)
    Call<Void> deleteAbastecimento(@Body Abastecimento abastecimento);

    //------------------------------------------------------

    @GET("registros/lembretes")
    Call<List<Lembrete>> getLembretes();

    @GET("registro/lembrete/{startDate}/{endDate}")
    Call<List<Lembrete>> getLembretesBetweenDates(@Path("startDate") String startDate, @Path("endDate") String endDate);

    @GET("registro/lembrete/{id}")
    Call<Lembrete> getLembreteById(@Path("id") int id);

    @POST("registro/lembrete")
    Call<Lembrete> insertLembrete(@Body Lembrete lembrete);

    @PUT("registro/lembrete")
    Call<Lembrete> updateLembrete(@Body Lembrete lembrete);


    @HTTP(method = "DELETE", path = "registro/lembrete", hasBody = true)
    Call<Void> deleteLembrete(@Body Lembrete lembrete);

    //------------------------------------------------------

    @GET("registros/manutencoes")
    Call<List<Manutencao>> getManutencoes();

    @GET("registro/manutencao/{startDate}/{endDate}")
    Call<List<Manutencao>> getManutencoesBetweenDates(@Path("startDate") String startDate, @Path("endDate") String endDate);

    @GET("registro/manutencao/{id}")
    Call<Manutencao> getManutencaoById(@Path("id") int id);

    @POST("registro/manutencao")
    Call<Manutencao> insertManutencao(@Body Manutencao manutencao);

    @PUT("registro/manutencao")
    Call<Manutencao> updateManutencao(@Body Manutencao manutencao);

    //@DELETE("registro/manutencao")
    @HTTP(method = "DELETE", path = "registro/manutencao", hasBody = true)
    Call<Void> deleteManutencao(@Body Manutencao manutencao);

}
