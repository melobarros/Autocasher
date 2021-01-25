package com.melobarros.autocasher.fragment;


import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.melobarros.autocasher.R;
import com.melobarros.autocasher.adapter.AdapterManutencao;
import com.melobarros.autocasher.model.Manutencao;
import com.melobarros.autocasher.services.autocasherAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 */
public class ManutencaoFragment extends Fragment {
    private static final String TAG = "ManutencaoFragment";

    private List<Manutencao> manutencoes = new ArrayList<>();

    Retrofit retrofit;
    autocasherAPI autocasherAPI;

    private RecyclerView recyclerManutencao;
    private AdapterManutencao adapterManutencao;





    public ManutencaoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initService();
        Log.d(TAG, "onCreateView: started.");
        View view = inflater.inflate(R.layout.fragment_manutencao, container, false);

        recyclerManutencao = view.findViewById(R.id.recyclerManutencao);
        initManutencoes();

        return view;
    }

    private void initManutencoes(){
        Log.d(TAG, "initManutencoes: fetching manutencoes list");

        Call<List<Manutencao>> requestManutencoes = autocasherAPI.getManutencoes();
        requestManutencoes.enqueue(new Callback<List<Manutencao>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<List<Manutencao>> call, Response<List<Manutencao>> response) {
                if(!response.isSuccessful()){
                    Log.v(TAG, "Erro400: " + response.message());
                    return;
                } else{
                    Log.d(TAG, "Setting variable list");

                    manutencoes = response.body();
                    orderList(manutencoes);
                    setupRecycler();
                }
            }

            @Override
            public void onFailure(Call<List<Manutencao>> call, Throwable t) {
                Log.e(TAG, "Erro Failure: " + t.getMessage());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setupRecycler(){
        recyclerManutencao.setHasFixedSize(true);
        recyclerManutencao.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapterManutencao = new AdapterManutencao(manutencoes, getActivity());
        recyclerManutencao.setAdapter(adapterManutencao);
        adapterManutencao.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void orderList(List<Manutencao> manutencoes){
        List<Manutencao> list = manutencoes;

        Collections.sort(list, (x, y) -> x.getLocalDateTime().compareTo(y.getLocalDateTime()));
        Collections.reverse(list);
    }

    private void initService(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override public void log(String message) {
                Log.d(TAG, "OkHttp: " + message);
            }
        });
        interceptor.level(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();


        retrofit = new Retrofit.Builder()
                .baseUrl(autocasherAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        autocasherAPI = retrofit.create(com.melobarros.autocasher.services.autocasherAPI.class);
    }

}
