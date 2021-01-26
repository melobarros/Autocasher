package com.melobarros.autocasher.fragment;


import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.melobarros.autocasher.R;
import com.melobarros.autocasher.adapter.AdapterManutencao;
import com.melobarros.autocasher.model.Abastecimento;
import com.melobarros.autocasher.model.Manutencao;
import com.melobarros.autocasher.services.autocasherAPI;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
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
    private AdapterManutencao adapterAbastecimento;
    public FloatingActionButton fab;

    public AbastecimentoFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();

        //adapterAbastecimento = new AdapterAbastecimento(abastecimentos, getActivity());
        //recyclerAbastecimento.setAdapter(adapterAbastecimento);
        //initAbastecimentos();
        //adapterAbastecimento.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        initService();
        Log.d(TAG, "onCreateView: started.");
        View view = inflater.inflate(R.layout.fragment_abastecimento, container, false);

        recyclerAbastecimento = view.findViewById(R.id.recyclerAbastecimento);
        fab = view.findViewById(R.id.novoAbastecimento_FAB);
        //initAbastecimentos();

        return view;
    }

    private void initAbastecimentos(){

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
