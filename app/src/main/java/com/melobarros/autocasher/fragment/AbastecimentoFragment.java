package com.melobarros.autocasher.fragment;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.melobarros.autocasher.R;
import com.melobarros.autocasher.activity.EditarAbastecimentoActivity;
import com.melobarros.autocasher.adapter.AdapterAbastecimento;
import com.melobarros.autocasher.adapter.AdapterManutencao;
import com.melobarros.autocasher.model.Abastecimento;
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
public class AbastecimentoFragment extends Fragment {
    private static final String TAG = "AbastecimentoFragment";

    private List<Abastecimento> abastecimentos = new ArrayList<>();

    Retrofit retrofit;
    autocasherAPI autocasherAPI;

    private RecyclerView recyclerAbastecimento;
    private AdapterAbastecimento adapterAbastecimento;
    public FloatingActionButton fab;
    Toolbar toolbar;

    public AbastecimentoFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();

        adapterAbastecimento = new AdapterAbastecimento(abastecimentos, getActivity());
        recyclerAbastecimento.setAdapter(adapterAbastecimento);
        initAbastecimentos();
        adapterAbastecimento.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        initService();
        Log.d(TAG, "onCreateView: started.");
        View view = inflater.inflate(R.layout.fragment_abastecimento, container, false);

        recyclerAbastecimento = view.findViewById(R.id.recyclerAbastecimento);
        fab = view.findViewById(R.id.novoAbastecimento_FAB);
        toolbar = view.findViewById(R.id.Abastecimento_toolbar);

        initAbastecimentos();
        initToolbar();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Abastecimento abastecimento = null;

                Intent i = new Intent(v.getContext(), EditarAbastecimentoActivity.class);
                i.putExtra("Abastecimento", abastecimento);
                v.getContext().startActivity(i);
            }
        });

        return view;
    }



    private void initAbastecimentos(){
        Log.d(TAG, "initAbastecimentos: fetching abastecimentos list");

        Call<List<Abastecimento>> requestAbastecimentos = autocasherAPI.getAbastecimentos();
        requestAbastecimentos.enqueue(new Callback<List<Abastecimento>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<List<Abastecimento>> call, Response<List<Abastecimento>> response) {
                if(!response.isSuccessful()){
                    Log.v(TAG, "Erro400: " + response.message());
                    return;
                } else{
                    Log.d(TAG, "Setting variable list");

                    abastecimentos = response.body();
                    orderList(abastecimentos);
                    setupRecycler();
                }
            }

            @Override
            public void onFailure(Call<List<Abastecimento>> call, Throwable t) {
                Log.e(TAG, "Erro Failure: " + t.getMessage());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void orderList(List<Abastecimento> abastecimentos){
        List<Abastecimento> list = abastecimentos;

        Collections.sort(list, (x, y) -> x.getLocalDateTime().compareTo(y.getLocalDateTime()));
        Collections.reverse(list);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setupRecycler(){
        recyclerAbastecimento.setHasFixedSize(true);
        recyclerAbastecimento.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapterAbastecimento = new AdapterAbastecimento(abastecimentos, getActivity());
        recyclerAbastecimento.setAdapter(adapterAbastecimento);
        adapterAbastecimento.notifyDataSetChanged();
    }

    public void initToolbar(){
        toolbar.setTitle("ABASTECIMENTO");
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        //((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
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
