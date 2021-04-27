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
import com.melobarros.autocasher.activity.EditarLembreteActivity;
import com.melobarros.autocasher.activity.EditarManutencaoActivity;
import com.melobarros.autocasher.adapter.AdapterLembrete;
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
public class LembreteFragment extends Fragment implements AdapterView.OnItemSelectedListener  {
    private static final String TAG = "LembreteFragment";

    private List<Lembrete> lembretes = new ArrayList<>();

    Retrofit retrofit;
    com.melobarros.autocasher.services.autocasherAPI autocasherAPI;

    private RecyclerView recyclerLembrete;
    private AdapterLembrete adapterLembrete;
    public FloatingActionButton fab;
    Toolbar toolbar;

    private Spinner ordenarPor_spinner, periodo_spinner;
    private static final String[] ordernarPor_paths = {"Ordernar por", "Mais novos", "Mais antigos", "Maior valor", "Menor valor"};
    private static final String[] periodo_paths = {"Período", "15 dias", "30 dias", "90 dias", "1 ano", "2 anos", "5 anos"};
    String selectedSpinner;

    public LembreteFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();

        adapterLembrete = new AdapterLembrete(lembretes, getActivity());
        recyclerLembrete.setAdapter(adapterLembrete);
        initLembretesBetweenDates(null, null);
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
        toolbar = view.findViewById(R.id.Lembrete_toolbar);

        ordenarPor_spinner = view.findViewById(R.id.ordenarPor_lembrete_spinner);
        periodo_spinner = view.findViewById(R.id.periodo_lembrete_spinner);

        //initLembretes();
        initToolbar();
        initSpinners();
        periodo_spinner.setSelection(1);
        ordenarPor_spinner.setSelection(1);
        initLembretesBetweenDates(null, null);

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

    public void initToolbar(){
        toolbar.setTitle("Lembrete");
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
    public void orderList(List<Lembrete> lembretes){
        String selectedOrder = ordenarPor_spinner.getSelectedItem().toString();
        List<Lembrete> list = lembretes;

        switch (selectedOrder){
            case "Ordernar por":
            case "Mais novos":
                Collections.sort(list, (x, y) -> x.getLocalDateTime().compareTo(y.getLocalDateTime()));
                Collections.reverse(list);
                adapterLembrete.notifyDataSetChanged();
                break;
            case "Mais antigos":
                Collections.sort(list, (x, y) -> x.getLocalDateTime().compareTo(y.getLocalDateTime()));
                adapterLembrete.notifyDataSetChanged();
                break;
            case "Maior valor":
                Collections.sort(list, new Comparator<Lembrete>() {
                    @Override
                    public int compare(Lembrete o1, Lembrete o2) {
                        return Float.compare(o1.getValorPrevisto(), o2.getValorPrevisto());
                    }
                });

                Collections.reverse(list);
                adapterLembrete.notifyDataSetChanged();
                break;
            case "Menor valor":
                Collections.sort(list, new Comparator<Lembrete>() {
                    @Override
                    public int compare(Lembrete o1, Lembrete o2) {
                        return Float.compare(o1.getValorPrevisto(), o2.getValorPrevisto());
                    }
                });
                adapterLembrete.notifyDataSetChanged();
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

    private void initLembretesBetweenDates(String _startDate, String _endDate){
        Log.d(TAG, "initLembretesBetweenDates: fetching lembretes list");

        String startDate = getStartDate(_startDate);
        String endDate = _endDate;

        if(endDate == null){
            SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");

            Calendar c = Calendar.getInstance();
            c.add(Calendar.YEAR, 1);
            c.getTimeInMillis();
            endDate = s.format(new Date(c.getTimeInMillis()));
        }

        Call<List<Lembrete>> requestLembrete = autocasherAPI.getLembretesBetweenDates(startDate, endDate);
        requestLembrete.enqueue(new Callback<List<Lembrete>>() {
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
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedSpinner = parent.getItemAtPosition(position).toString();

        for (String order : ordernarPor_paths) {
            if(selectedSpinner == order){
                orderList(lembretes);
            }
        }

        for (String period : periodo_paths) {
            if(selectedSpinner == period){
                initLembretesBetweenDates(null, null);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
