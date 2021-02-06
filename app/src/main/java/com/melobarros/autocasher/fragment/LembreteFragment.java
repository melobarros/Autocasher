package com.melobarros.autocasher.fragment;


import android.content.Intent;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.melobarros.autocasher.R;
import com.melobarros.autocasher.activity.EditarLembreteActivity;
import com.melobarros.autocasher.activity.EditarManutencaoActivity;
import com.melobarros.autocasher.adapter.AdapterLembrete;
import com.melobarros.autocasher.adapter.AdapterManutencao;
import com.melobarros.autocasher.model.Lembrete;
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
public class LembreteFragment extends Fragment {
    private static final String TAG = "LembreteFragment";

    private List<Lembrete> lembretes = new ArrayList<>();

    Retrofit retrofit;
    com.melobarros.autocasher.services.autocasherAPI autocasherAPI;

    private RecyclerView recyclerLembrete;
    private AdapterLembrete adapterLembrete;
    public FloatingActionButton fab;


    public LembreteFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();

        adapterLembrete = new AdapterLembrete(lembretes, getActivity());
        recyclerLembrete.setAdapter(adapterLembrete);
        initLembretes();
        adapterLembrete.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initService();
        Log.d(TAG, "onCreateView: started.");
        View view = inflater.inflate(R.layout.fragment_lembrete, container, false);

        recyclerLembrete = view.findViewById(R.id.recyclerLembrete);
        fab = view.findViewById(R.id.novoLembrete_FAB);
        initLembretes();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Lembrete lembrete = null;

                Intent i = new Intent(v.getContext(), EditarLembreteActivity.class);
                i.putExtra("Lembrete", lembrete);
                v.getContext().startActivity(i);
            }
        });

        return view;
    }

    private void initLembretes(){
        Log.d(TAG, "initLembretes: fetching lembretes list");

        Call<List<Lembrete>> requestLembretes = autocasherAPI.getLembretes();
        requestLembretes.enqueue(new Callback<List<Lembrete>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<List<Lembrete>> call, Response<List<Lembrete>> response) {
                if(!response.isSuccessful()){
                    Log.v(TAG, "Erro400: " + response.message());
                    return;
                } else{
                    Log.d(TAG, "Setting variable list");

                    lembretes = response.body();
                    orderList(lembretes);
                    setupRecycler();
                }
            }

            @Override
            public void onFailure(Call<List<Lembrete>> call, Throwable t) {
                Log.e(TAG, "Erro Failure: " + t.getMessage());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setupRecycler(){
        recyclerLembrete.setHasFixedSize(true);
        recyclerLembrete.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapterLembrete = new AdapterLembrete(lembretes, getActivity());
        recyclerLembrete.setAdapter(adapterLembrete);
        adapterLembrete.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void orderList(List<Lembrete> lembretes){
        List<Lembrete> list = lembretes;

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
