package com.melobarros.autocasher.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.melobarros.autocasher.MainActivity;
import com.melobarros.autocasher.R;
import com.melobarros.autocasher.model.Gasto;
import com.melobarros.autocasher.services.autocasherAPI;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 */
public class GastoFragment extends Fragment {
    private static final String TAG = "GastoFragment";

    private List<Gasto> gastos = new ArrayList<>();

    Retrofit retrofit;
    autocasherAPI autocasherAPI;


    public GastoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(autocasherAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        autocasherAPI = retrofit.create(com.melobarros.autocasher.services.autocasherAPI.class);

        Log.d(TAG, "onCreateView: started.");

        //initGastos();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gasto, container, false);
    }

    private void initGastos(){
        Log.d(TAG, "initGastos: fetching gastos list");

        Call<List<Gasto>> requestGastos = autocasherAPI.getGastos();
        requestGastos.enqueue(new Callback<List<Gasto>>(){

            @Override
            public void onResponse(Call<List<Gasto>> call, Response<List<Gasto>> response) {
                if(!response.isSuccessful()){
                    Log.e(TAG, "Erro: " + response.code());
                    return;
                } else{
                    gastos = response.body();

                    Gasto g = gastos.get(0);

                    Toast.makeText(getActivity(), String.valueOf(g.getId()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Gasto>> call, Throwable t) {
                Log.e(TAG, "Erro: " + t.getMessage());
            }
        });
    }

}
