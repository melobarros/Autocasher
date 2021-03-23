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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.melobarros.autocasher.MainActivity;
import com.melobarros.autocasher.R;
import com.melobarros.autocasher.activity.CalcularMelhorCombustivelActivity;
import com.melobarros.autocasher.activity.EditarAbastecimentoActivity;
import com.melobarros.autocasher.adapter.AdapterAbastecimento;
import com.melobarros.autocasher.adapter.AdapterManutencao;
import com.melobarros.autocasher.model.Abastecimento;
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
public class AbastecimentoFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "AbastecimentoFragment";

    private List<Abastecimento> abastecimentos = new ArrayList<>();

    Retrofit retrofit;
    autocasherAPI autocasherAPI;

    private RecyclerView recyclerAbastecimento;
    private AdapterAbastecimento adapterAbastecimento;
    public FloatingActionButton novoAbastecimentoFab, calcularMelhorCombustivelFab;
    Toolbar toolbar;

    private Spinner ordenarPor_spinner, periodo_spinner;
    private static final String[] ordernarPor_paths = {"Ordernar por", "Mais novos", "Mais antigos", "Maior valor", "Menor valor"};
    private static final String[] periodo_paths = {"Período", "15 dias", "30 dias", "90 dias", "1 ano", "2 anos", "5 anos"};
    String selectedSpinner;


    public AbastecimentoFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();

        adapterAbastecimento = new AdapterAbastecimento(abastecimentos, getActivity());
        recyclerAbastecimento.setAdapter(adapterAbastecimento);
        initAbastecimentosBetweenDates(null, null);
        adapterAbastecimento.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        initService();
        Log.d(TAG, "onCreateView: started.");
        View view = inflater.inflate(R.layout.fragment_abastecimento, container, false);

        initComponentes(view);
        initToolbar();
        initSpinners();
        initAbastecimentosBetweenDates(null, null);
        //todos


        novoAbastecimentoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Abastecimento abastecimento = null;

                Intent i = new Intent(v.getContext(), EditarAbastecimentoActivity.class);
                i.putExtra("Abastecimento", abastecimento);
                v.getContext().startActivity(i);
            }
        });

        calcularMelhorCombustivelFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), CalcularMelhorCombustivelActivity.class);
                v.getContext().startActivity(i);
            }
        });

        return view;
    }

    private void initComponentes(View view){
        recyclerAbastecimento = view.findViewById(R.id.recyclerAbastecimento);
        novoAbastecimentoFab = view.findViewById(R.id.novoAbastecimento_FAB);
        calcularMelhorCombustivelFab = view.findViewById(R.id.calcularMelhorCombustivel_FAB);
        toolbar = view.findViewById(R.id.Abastecimento_toolbar);
        ordenarPor_spinner = view.findViewById(R.id.ordenarPor_abastecimento_spinner);
        periodo_spinner = view.findViewById(R.id.periodo_abastecimento_spinner);
    }

    private void initSpinners(){
        ArrayAdapter<String>adapterOrdenar = new ArrayAdapter<>(getActivity(),
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

    private void initAbastecimentosBetweenDates(String _startDate, String _endDate){
        Log.d(TAG, "initAbastecimentosBetweenDates: fetching abastecimentos list");

        String startDate = getStartDate(_startDate);
        String endDate = _endDate;

        if(endDate == null){
            SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
            endDate = s.format(new Date(Calendar.getInstance().getTimeInMillis()));
        }

        Call<List<Abastecimento>> requestAbastecimentos = autocasherAPI.getAbastecimentosBetweenDates(startDate, endDate);
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
    public void setupRecycler(){
        recyclerAbastecimento.setHasFixedSize(true);
        recyclerAbastecimento.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapterAbastecimento = new AdapterAbastecimento(abastecimentos, getActivity());
        recyclerAbastecimento.setAdapter(adapterAbastecimento);
        adapterAbastecimento.notifyDataSetChanged();
    }

    public void initToolbar(){
        toolbar.setTitle("");
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void orderList(List<Abastecimento> abastecimentos){
        String selectedOrder = ordenarPor_spinner.getSelectedItem().toString();
        List<Abastecimento> list = abastecimentos;

        switch (selectedOrder){
            case "Ordernar por":
            case "Mais novos":
                Collections.sort(list, (x, y) -> x.getLocalDateTime().compareTo(y.getLocalDateTime()));
                Collections.reverse(list);
                adapterAbastecimento.notifyDataSetChanged();
                break;
            case "Mais antigos":
                Collections.sort(list, (x, y) -> x.getLocalDateTime().compareTo(y.getLocalDateTime()));
                adapterAbastecimento.notifyDataSetChanged();
                break;
            case "Maior valor":
                Collections.sort(list, new Comparator<Abastecimento>() {
                    @Override
                    public int compare(Abastecimento o1, Abastecimento o2) {
                        return Float.compare(o1.getLitros() * o1.getPrecoLitro(), o2.getLitros() * o2.getPrecoLitro());
                    }
                });

                Collections.reverse(list);
                adapterAbastecimento.notifyDataSetChanged();
                break;
            case "Menor valor":
                Collections.sort(list, new Comparator<Abastecimento>() {
                    @Override
                    public int compare(Abastecimento o1, Abastecimento o2) {
                        return Float.compare(o1.getLitros() * o1.getPrecoLitro(), o2.getLitros() * o2.getPrecoLitro());
                    }
                });
                adapterAbastecimento.notifyDataSetChanged();
                break;
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Toast.makeText(getActivity(), "YOUR SELECTION IS : " + parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();

        selectedSpinner = parent.getItemAtPosition(position).toString();

        //private static final String[] ordernarPor_paths = {"Ordernar por", "Mais novos", "Mais antigos", "Maior valor", "Menor valor"};
        //private static final String[] periodo_paths = {"Período", "15 dias", "30 dias", "90 dias", "1 ano", "2 anos", "5 anos"};

        for (String order : ordernarPor_paths) {
            if(selectedSpinner == order){
                orderList(abastecimentos);
            }
        }

        for (String period : periodo_paths) {
            if(selectedSpinner == period){
                initAbastecimentosBetweenDates(null, null);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
