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
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.melobarros.autocasher.MainActivity;
import com.melobarros.autocasher.R;
import com.melobarros.autocasher.activity.EditarGastoActivity;
import com.melobarros.autocasher.adapter.AdapterGasto;
import com.melobarros.autocasher.model.Abastecimento;
import com.melobarros.autocasher.model.Gasto;
import com.melobarros.autocasher.services.autocasherAPI;
import com.melobarros.autocasher.utils.SpacingDecorator_Gasto;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
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
import java.time.LocalDateTime;

/**
 * A simple {@link Fragment} subclass.
 */
public class GastoFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "GastoFragment";

    private List<Gasto> gastos = new ArrayList<>();

    Retrofit retrofit;
    autocasherAPI autocasherAPI;

    private RecyclerView recyclerGasto;
    private AdapterGasto adapterGasto;
    public FloatingActionButton fab;
    Toolbar toolbar;

    private Spinner ordenarPor_spinner, periodo_spinner;
    private static final String[] ordernarPor_paths = {"Ordernar por", "Mais novos", "Mais antigos", "Maior valor", "Menor valor"};
    private static final String[] periodo_paths = {"Período", "15 dias", "30 dias", "90 dias", "1 ano", "2 anos", "5 anos"};
    String selectedSpinner;

    public GastoFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();

        adapterGasto = new AdapterGasto(gastos, getActivity());
        recyclerGasto.setAdapter(adapterGasto);

        initGastosBetweenDates(null, null);
        adapterGasto.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        initService();

        Log.d(TAG, "onCreateView: started.");
        View view = inflater.inflate(R.layout.fragment_gasto, container, false);

        recyclerGasto = view.findViewById(R.id.recyclerGastos);
        fab = view.findViewById(R.id.novoGasto_FAB);
        toolbar = view.findViewById(R.id.Gasto_toolbar);
        ordenarPor_spinner = view.findViewById(R.id.ordenarPor_gasto_spinner);
        periodo_spinner = view.findViewById(R.id.periodo_gasto_spinner);


        initToolbar();
        initSpinners();
        initGastosBetweenDates(null, null);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Gasto gasto = null;

                Intent i = new Intent(v.getContext(), EditarGastoActivity.class);
                i.putExtra("Gasto", gasto);
                v.getContext().startActivity(i);
            }
        });

        return view;
    }



    private void initGastos(){
        Log.d(TAG, "initGastos: fetching gastos list");

        Call<List<Gasto>> requestGastos = autocasherAPI.getGastos();
        requestGastos.enqueue(new Callback<List<Gasto>>(){

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<List<Gasto>> call, Response<List<Gasto>> response) {
                if(!response.isSuccessful()){
                    if (response.code() == 400) {
                        Log.v(TAG, "Erro400: " + response.message());
                    } else{
                        Log.e(TAG, "ErroNotSuccessful: " + response.code());
                    }

                    return;
                } else{

                    Log.d(TAG, "Setting variable list");
                    gastos = response.body();

                    orderList(gastos);
                    setupRecycler();
                }
            }

            @Override
            public void onFailure(Call<List<Gasto>> call, Throwable t) {
                Log.e(TAG, "Erro Failure: " + t.getMessage());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setupRecycler(){
        recyclerGasto.setHasFixedSize(true);
        recyclerGasto.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapterGasto = new AdapterGasto(gastos, getActivity());
        recyclerGasto.setAdapter(adapterGasto);
        adapterGasto.notifyDataSetChanged();
    }

    public void initService(){
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

    public void initToolbar(){
        toolbar.setTitle("");
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
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
    public void orderList(List<Gasto> gastos){
        String selectedOrder = ordenarPor_spinner.getSelectedItem().toString();
        List<Gasto> list = gastos;

        switch (selectedOrder){
            case "Ordernar por":
            case "Mais novos":
                Collections.sort(list, (x, y) -> x.getLocalDateTime().compareTo(y.getLocalDateTime()));
                Collections.reverse(list);
                adapterGasto.notifyDataSetChanged();
                break;
            case "Mais antigos":
                Collections.sort(list, (x, y) -> x.getLocalDateTime().compareTo(y.getLocalDateTime()));
                adapterGasto.notifyDataSetChanged();
                break;
            case "Maior valor":
                Collections.sort(list, new Comparator<Gasto>() {
                    @Override
                    public int compare(Gasto o1, Gasto o2) {
                        return Float.compare(o1.getValorTotal(), o2.getValorTotal());
                    }
                });

                Collections.reverse(list);
                adapterGasto.notifyDataSetChanged();
                break;
            case "Menor valor":
                Collections.sort(list, new Comparator<Gasto>() {
                    @Override
                    public int compare(Gasto o1, Gasto o2) {
                        return Float.compare(o1.getValorTotal(), o2.getValorTotal());
                    }
                });
                adapterGasto.notifyDataSetChanged();
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

    private void initGastosBetweenDates(String _startDate, String _endDate){
        Log.d(TAG, "initGastosBetweenDates: fetching gastos list");

        String startDate = getStartDate(_startDate);
        String endDate = _endDate;

        if(endDate == null){
            SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
            endDate = s.format(new Date(Calendar.getInstance().getTimeInMillis()));
        }

        Call<List<Gasto>> requestGastos = autocasherAPI.getGastosBetweenDates(startDate, endDate);
        requestGastos.enqueue(new Callback<List<Gasto>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<List<Gasto>> call, Response<List<Gasto>> response) {
                if(!response.isSuccessful()){
                    Log.v(TAG, "Erro400: " + response.message());
                    return;
                } else{
                    Log.d(TAG, "Setting variable list");

                    gastos = response.body();
                    orderList(gastos);
                    setupRecycler();
                }
            }

            @Override
            public void onFailure(Call<List<Gasto>> call, Throwable t) {
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
                orderList(gastos);
            }
        }

        for (String period : periodo_paths) {
            if(selectedSpinner == period){
                initGastosBetweenDates(null, null);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
