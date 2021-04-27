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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.melobarros.autocasher.R;
import com.melobarros.autocasher.activity.EditarGastoActivity;
import com.melobarros.autocasher.activity.EditarManutencaoActivity;
import com.melobarros.autocasher.adapter.AdapterManutencao;
import com.melobarros.autocasher.model.Gasto;
import com.melobarros.autocasher.model.Lembrete;
import com.melobarros.autocasher.model.Manutencao;
import com.melobarros.autocasher.services.autocasherAPI;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
public class ManutencaoFragment extends Fragment implements AdapterView.OnItemSelectedListener  {
    private static final String TAG = "ManutencaoFragment";

    private List<Manutencao> manutencoes = new ArrayList<>();

    Retrofit retrofit;
    autocasherAPI autocasherAPI;

    private RecyclerView recyclerManutencao;
    private AdapterManutencao adapterManutencao;
    public FloatingActionButton fab;
    Toolbar toolbar;

    private Spinner ordenarPor_spinner, periodo_spinner;
    private static final String[] ordernarPor_paths = {"Ordernar por", "Mais novos", "Mais antigos", "Maior valor", "Menor valor"};
    private static final String[] periodo_paths = {"Período", "15 dias", "30 dias", "90 dias", "1 ano", "2 anos", "5 anos"};
    String selectedSpinner;

    public ManutencaoFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();

        adapterManutencao = new AdapterManutencao(manutencoes, getActivity());
        recyclerManutencao.setAdapter(adapterManutencao);
        initManutencoesBetweenDates(null, null);
        adapterManutencao.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initService();
        Log.d(TAG, "onCreateView: started.");
        View view = inflater.inflate(R.layout.fragment_manutencao, container, false);

        recyclerManutencao = view.findViewById(R.id.recyclerManutencao);
        fab = view.findViewById(R.id.novoManutencao_FAB);
        toolbar = view.findViewById(R.id.Manutencao_toolbar);

        ordenarPor_spinner = view.findViewById(R.id.ordenarPor_manutencao_spinner);
        periodo_spinner = view.findViewById(R.id.periodo_manutencao_spinner);

        initToolbar();
        initSpinners();
        periodo_spinner.setSelection(1);
        ordenarPor_spinner.setSelection(1);
        initManutencoesBetweenDates(null, null);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Manutencao manutencao = null;

                Intent i = new Intent(v.getContext(), EditarManutencaoActivity.class);
                i.putExtra("Manutencao", manutencao);
                v.getContext().startActivity(i);
            }
        });

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


    public void initToolbar(){
        toolbar.setTitle("Manutenção");
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    private void initSpinners(){
        ArrayAdapter<String> adapterOrdenar = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item,ordernarPor_paths);
        ArrayAdapter<String>adapterPeriodo = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item,periodo_paths);

        adapterOrdenar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterPeriodo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ordenarPor_spinner.setAdapter(adapterOrdenar);
        ordenarPor_spinner.setOnItemSelectedListener(this);
        periodo_spinner.setAdapter(adapterPeriodo);
        periodo_spinner.setOnItemSelectedListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void orderList(List<Manutencao> manutencoes){
        String selectedOrder = ordenarPor_spinner.getSelectedItem().toString();
        List<Manutencao> list = manutencoes;

        switch (selectedOrder){
            case "Ordernar por":
            case "Mais novos":
                Collections.sort(list, (x, y) -> x.getLocalDateTime().compareTo(y.getLocalDateTime()));
                Collections.reverse(list);
                adapterManutencao.notifyDataSetChanged();
                break;
            case "Mais antigos":
                Collections.sort(list, (x, y) -> x.getLocalDateTime().compareTo(y.getLocalDateTime()));
                adapterManutencao.notifyDataSetChanged();
                break;
            case "Maior valor":
                Collections.sort(list, new Comparator<Manutencao>() {
                    @Override
                    public int compare(Manutencao o1, Manutencao o2) {
                        return Float.compare(o1.getValor(), o2.getValor());
                    }
                });

                Collections.reverse(list);
                adapterManutencao.notifyDataSetChanged();
                break;
            case "Menor valor":
                Collections.sort(list, new Comparator<Manutencao>() {
                    @Override
                    public int compare(Manutencao o1, Manutencao o2) {
                        return Float.compare(o1.getValor(), o2.getValor());
                    }
                });
                adapterManutencao.notifyDataSetChanged();
                break;
        }

    }

    public static String getCalculatedDate(int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
        cal.add(Calendar.DAY_OF_YEAR, days);
        return s.format(new Date(cal.getTimeInMillis()));
    }

    private String getStartDate(String _startDate){

        String startDate = _startDate;
        String selectedPeriodo = periodo_spinner.getSelectedItem().toString();

        if(startDate == null){
            switch (selectedPeriodo) {
                case "Período":
                case "15 dias":
                    startDate = getCalculatedDate(-15);
                    break;
                case "30 dias":
                    startDate = getCalculatedDate(-30);
                    break;
                case "90 dias":
                    startDate = getCalculatedDate(-90);
                    break;
                case "1 ano":
                    startDate = getCalculatedDate(-365);
                    break;
                case "2 anos":
                    startDate = getCalculatedDate(-365*2);
                    break;
                case "5 anos":
                    startDate = getCalculatedDate(-365*5);
                    break;
            }
        }

        return startDate;
    }

    private void initManutencoesBetweenDates(String _startDate, String _endDate){
        Log.d(TAG, "initManutencoesBetweenDates: fetching manutencoes list");

        String startDate = getStartDate(_startDate);
        String endDate = _endDate;

        if(endDate == null){
            SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
            endDate = s.format(new Date(Calendar.getInstance().getTimeInMillis()));
        }

        Call<List<Manutencao>> requestManutencao = autocasherAPI.getManutencoesBetweenDates(startDate, endDate);
        requestManutencao.enqueue(new Callback<List<Manutencao>>() {
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
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedSpinner = parent.getItemAtPosition(position).toString();

        for (String order : ordernarPor_paths) {
            if(selectedSpinner == order){
                orderList(manutencoes);
            }
        }

        for (String period : periodo_paths) {
            if(selectedSpinner == period){
                initManutencoesBetweenDates(null, null);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
