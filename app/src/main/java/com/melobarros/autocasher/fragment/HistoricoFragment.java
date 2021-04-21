package com.melobarros.autocasher.fragment;


import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melobarros.autocasher.R;
import com.melobarros.autocasher.adapter.AdapterGasto;
import com.melobarros.autocasher.model.Abastecimento;
import com.melobarros.autocasher.model.Gasto;
import com.melobarros.autocasher.model.Lembrete;
import com.melobarros.autocasher.model.Manutencao;
import com.melobarros.autocasher.model.Registro;
import com.melobarros.autocasher.services.autocasherAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoricoFragment extends Fragment implements AdapterView.OnItemSelectedListener  {
    private static final String TAG = "HistoricoFragment";

    private List<Registro> registros = new ArrayList<>();
    private List<Abastecimento> abastecimentos = new ArrayList<>();
    private List<Gasto> gastos = new ArrayList<>();
    private List<Lembrete> lembretes = new ArrayList<>();
    private List<Manutencao> manutencoes = new ArrayList<>();
    Toolbar toolbar;

    Retrofit retrofit;
    Retrofit retrofit_scalar;
    com.melobarros.autocasher.services.autocasherAPI autocasherAPI;
    com.melobarros.autocasher.services.autocasherAPI autocasherAPI_scalar;

    Gson gson = new Gson();

    private Spinner periodo_spinner;
    private static final String[] periodo_paths = {"Período", "15 dias", "30 dias", "90 dias", "1 ano", "2 anos", "5 anos"};
    String selectedSpinner;

    TextView abastecimento_qtde, manutencao_qtde, lembrete_qtde, gasto_qtde, totalDespesas, precoMedioLitro, consumoMedio;
    BarChart gastosBarChart, gastosTipoBarChart;

    public HistoricoFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();

        //initRegistrosBetweenDates(null, null);
        Log.d(TAG, "initRegistros: onResume");
        initRegistrosBetweenDates_scalar(null, null);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: started.");
        View view = inflater.inflate(R.layout.fragment_historico, container, false);

        toolbar = view.findViewById(R.id.Estatisticas_toolbar);
        periodo_spinner = view.findViewById(R.id.periodo_historico_spinner);

        initStatsElements(view);
        initToolbar();
        initService();
        initService_scalar();
        initSpinners();
        //initRegistrosBetweenDates(null, null);
        Log.d(TAG, "initRegistros: onCreateView");
        initRegistrosBetweenDates_scalar(null, null);

        return view;
    }

    public void initStatsElements(View view){
        abastecimento_qtde = view.findViewById(R.id.abastecimentos_qtde_textView);
        manutencao_qtde = view.findViewById(R.id.manutencoes_qtde_textView);
        lembrete_qtde = view.findViewById(R.id.lembretes_qtde_textView);
        gasto_qtde = view.findViewById(R.id.gastos_qtde_textView);
        gastosBarChart = view.findViewById(R.id.gastosMes_barChart);
        gastosTipoBarChart = view.findViewById(R.id.gastosTipo_barChart);
        totalDespesas = view.findViewById(R.id.despesasTotaisValor_textView);
        consumoMedio = view.findViewById(R.id.consumoMedio_textView);
        precoMedioLitro = view.findViewById(R.id.precoMedioLitroValor_textView);
    }

    public void initToolbar(){
        toolbar.setTitle("");
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
    }

    public void initService_scalar(){
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


        retrofit_scalar = new Retrofit.Builder()
                .baseUrl(autocasherAPI.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(client)
                .build();

        autocasherAPI_scalar = retrofit_scalar.create(com.melobarros.autocasher.services.autocasherAPI.class);
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

    private void initSpinners(){
        ArrayAdapter<String>adapterPeriodo = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item,periodo_paths);
        adapterPeriodo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        periodo_spinner.setAdapter(adapterPeriodo);
        periodo_spinner.setOnItemSelectedListener(this);
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

    public void setObjectsToLists(String response) throws JSONException {
        JSONObject registro;
        String tipo;
        JsonParser jsonParser;
        JsonObject gsonObject;
        JSONArray jsonArray;

        abastecimentos.clear();
        gastos.clear();
        lembretes.clear();
        manutencoes.clear();

        jsonArray = new JSONArray(response);
        for (int i = 0; i < jsonArray.length(); i++) {
            registro = jsonArray.getJSONObject(i);
            tipo = registro.getString("tipo");
            jsonParser = new JsonParser();
            gsonObject = (JsonObject)jsonParser.parseString(registro.toString());

            switch (tipo) {
                case "Abastecimento":
                    abastecimentos.add(gson.fromJson(gsonObject, Abastecimento.class));
                    break;
                case "Gasto":
                    gastos.add(gson.fromJson(gsonObject, Gasto.class));
                    break;
                case "Lembrete":
                    lembretes.add(gson.fromJson(gsonObject, Lembrete.class));
                    break;
                case "Manutencao":
                    manutencoes.add(gson.fromJson(gsonObject, Manutencao.class));
                    break;
            }
        }
    }





    private void initRegistrosBetweenDates_scalar(String _startDate, String _endDate){
        Log.d(TAG, "initRegistrosBetweenDates_scalar: fetching Registros list");

        String startDate = getStartDate(_startDate);
        String endDate = _endDate;

        if(endDate == null){
            SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
            endDate = s.format(new Date(Calendar.getInstance().getTimeInMillis()));
        }

        Call<String> requestRegistros = autocasherAPI_scalar.getRegistrosBetweenDatesAsString(startDate, endDate);
        requestRegistros.enqueue(new Callback<String>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(!response.isSuccessful()){
                    Log.v(TAG, "Erro400: " + response.message());
                    return;
                } else{
                    Log.d(TAG, "Successful!");
                    Log.d(TAG, "Full response: " + response);
                    Log.d(TAG, "Response body: " + response.body());

                    try {
                        setObjectsToLists(response.body());
                        updateCharts();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //registros = response.body();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "Erro Failure Scalar: " + t.getMessage());
            }
        });
    }

    private void initRegistrosBetweenDates(String _startDate, String _endDate){
        Log.d(TAG, "initRegistrosBetweenDates: fetching Registros list");

        String startDate = getStartDate(_startDate);
        String endDate = _endDate;

        if(endDate == null){
            SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
            endDate = s.format(new Date(Calendar.getInstance().getTimeInMillis()));
        }

        Call<List<Registro>> requestRegistros = autocasherAPI.getRegistrosBetweenDates(startDate, endDate);
        requestRegistros.enqueue(new Callback<List<Registro>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<List<Registro>> call, Response<List<Registro>> response) {
                if(!response.isSuccessful()){
                    Log.v(TAG, "Erro400: " + response.message());
                    return;
                } else{
                    Log.d(TAG, "Setting variable list");

                    registros = response.body();
                }
            }

            @Override
            public void onFailure(Call<List<Registro>> call, Throwable t) {
                Log.e(TAG, "Erro Failure: " + t.getMessage());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedSpinner = parent.getItemAtPosition(position).toString();

        for (String period : periodo_paths) {
            if(selectedSpinner == period){
                Log.d(TAG, "initRegistros: onItemSelected");
                initRegistrosBetweenDates_scalar(null, null);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateCharts(){
        abastecimento_qtde.setText(String.valueOf(abastecimentos.size()));
        manutencao_qtde.setText(String.valueOf(manutencoes.size()));
        lembrete_qtde.setText(String.valueOf(lembretes.size()));
        gasto_qtde.setText(String.valueOf(gastos.size()));
        totalDespesas.setText("R$ " + String.format("%.02f", getDespesasTotais()));
        precoMedioLitro.setText("R$ " + String.format("%.02f", getPrecoMedioLitro()));

        if(!gastos.isEmpty()) {
            updateGastosMes();
            updateGastosTipo();
        }
    }

    private float getConsumoMedio(){
        //TODO
    }

    private float getPrecoMedioLitro(){
        float precoMedio = 0.0f;
        int contador = 0;

        for(Abastecimento a : abastecimentos){
            if(a.getPrecoLitro() > 0.0f){
                precoMedio = precoMedio + a.getPrecoLitro();
                contador = contador + 1;
            }
        }

        if(precoMedio > 0.0f){
            precoMedio = precoMedio / contador;
        }

        return precoMedio;
    }

    private float getDespesasTotais(){
        float despesasTotais = 0.0f;
        float valorAbastecimentoTemp;

        for(Gasto g : gastos){
            if(g.getValorTotal() > 0.0f){
                despesasTotais = despesasTotais + g.getValorTotal();
            }
        }

        for(Abastecimento a : abastecimentos){
            valorAbastecimentoTemp = a.getLitros() * a.getPrecoLitro();
            if(valorAbastecimentoTemp > 0.0f){
                despesasTotais = despesasTotais + valorAbastecimentoTemp;
            }
        }

        for(Manutencao m : manutencoes){
            if(m.getValor() > 0.0f){
                despesasTotais = despesasTotais + m.getValor();
            }
        }

        return despesasTotais;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateGastosMes(){
        BarData barData = new BarData(getGastosMesDataSet());
        List<String> labels = getLabelsMes("Gasto");
        setupBarChart(gastosBarChart, barData, labels);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateGastosTipo(){
        BarData barData = new BarData(getGastosTipoDataSet());
        List<String> labels = getLabelsTipoGasto();
        setupBarChart(gastosTipoBarChart, barData, labels);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupBarChart(BarChart barChart, BarData barData, List<String> labels){
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getAxisRight().setEnabled(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.setDrawValueAboveBar(true);
        barChart.getDescription().setEnabled(false);
        barChart.setData(barData);
        barChart.setTouchEnabled(false);
        barChart.animateXY(2000, 2000);
        XAxis bottomAxis = barChart.getXAxis();
        bottomAxis.setLabelCount(labels.size());

        barChart.invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private BarDataSet getGastosTipoDataSet(){
        List<BarEntry> barEntries = new ArrayList<BarEntry>();
        List<String> labels = getLabelsTipoGasto();
        Map<String, Float> gastoMap = getGastoTipoCountMap(labels);

        for(Map.Entry<String, Float> entry : gastoMap.entrySet()){
            String tipo = entry.getKey();
            Float valorTotal = entry.getValue();
            barEntries.add(new BarEntry(labels.indexOf(tipo), valorTotal));
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Gastos");
        barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        barDataSet.setColor(Color.rgb(21,48,97));
        barDataSet.setValueTextColor(Color.rgb(64,6,6));
        barDataSet.setValueTextSize(11f);

        barDataSet.setHighlightEnabled(false);

        return barDataSet;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private BarDataSet getGastosMesDataSet(){
        List<BarEntry> barEntries = new ArrayList<BarEntry>();
        List<String> labels = getLabelsMes("Gasto");
        Map<String, Float> gastoMap = getGastoYearMonthTotalValueMap(labels);

        for(Map.Entry<String, Float> entry : gastoMap.entrySet()){
            String yearMonth = entry.getKey();
            Float valorTotal = entry.getValue();
            barEntries.add(new BarEntry(labels.indexOf(yearMonth), valorTotal));
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Gastos");
        barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        barDataSet.setColor(Color.rgb(21,48,97));
        barDataSet.setValueTextColor(Color.rgb(64,6,6));
        barDataSet.setValueTextSize(11f);

        barDataSet.setHighlightEnabled(false);
        //barDataSet.setHighLightColor(Color.RED);
        //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        return barDataSet;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Map<String, Float> getGastoYearMonthTotalValueMap(List<String> labels){
        Map<String, Float> gastoYearMonthTotalValueMap = new HashMap<String, Float>();
        String yearMonth;
        SimpleDateFormat s = new SimpleDateFormat("MMM/yy");

        for(String l : labels){
            gastoYearMonthTotalValueMap.put(l, 0.0f);
        }

        for(Gasto g : gastos){
            //Date.from(g.getLocalDateTime().atZone(ZoneId.systemDefault()).toInstant())
            yearMonth = s.format(Date.from(g.getLocalDateTime().atZone(ZoneId.systemDefault()).toInstant()));
            gastoYearMonthTotalValueMap.put(yearMonth, gastoYearMonthTotalValueMap.get(yearMonth) + g.getValorTotal());
            Log.d(TAG, "##### Mes: " + yearMonth + " #### ValorTotal: " + g.getValorTotal() + " #### ListIndex: " + labels.indexOf(yearMonth));
            //map.put(key, map.get(key) + valueToAdd);
        }

        return gastoYearMonthTotalValueMap;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Map<String, Float> getGastoTipoCountMap(List<String> labels){
        Map<String, Float> gastoTipoCountMap = new HashMap<String, Float>();
        String tipoGasto;

        for(String l : labels){
            gastoTipoCountMap.put(l, 0.0f);
        }

        for(Gasto g : gastos){
            tipoGasto = standardString(g.getObservacao());
            gastoTipoCountMap.put(tipoGasto, gastoTipoCountMap.get(tipoGasto) + g.getValorTotal());
        }

        return gastoTipoCountMap;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<String> getLabelsTipoGasto(){
        List<String> tiposGastos = new ArrayList<String>();
        String tipoGasto;

        for(Gasto g : gastos){
            tipoGasto = standardString(g.getObservacao());

            if(!tiposGastos.contains(tipoGasto)){
                tiposGastos.add(tipoGasto);
            }
        }

        return tiposGastos;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<String> getLabelsMes(String tipo){
        List<Date> listaDatas = new ArrayList<Date>();

        switch (tipo) {
            case "Abastecimento":
                for(Abastecimento g : abastecimentos){ listaDatas.add(Date.from(g.getLocalDateTime().atZone(ZoneId.systemDefault()).toInstant())); }
                break;
            case "Gasto":
                for(Gasto g : gastos){ listaDatas.add(Date.from(g.getLocalDateTime().atZone(ZoneId.systemDefault()).toInstant())); }
                break;
            case "Lembrete":
                for(Lembrete g : lembretes){ listaDatas.add(Date.from(g.getLocalDateTime().atZone(ZoneId.systemDefault()).toInstant())); }
                break;
            case "Manutencao":
                for(Manutencao g : manutencoes){ listaDatas.add(Date.from(g.getLocalDateTime().atZone(ZoneId.systemDefault()).toInstant())); }
                break;
        }

        if(!listaDatas.isEmpty()){
            List<String> labels = getYearMonths(listaDatas);

            for(String l : labels){
                //Log.d(TAG, "##### Mes: " + l);
            }

            return labels;
        }


        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<String> getYearMonths(List<Date> datas){
        List<String> yearMonths = new ArrayList<String>();
        Collections.sort(datas, (x, y) -> x.compareTo(y));
        String yearMonth;
        SimpleDateFormat s = new SimpleDateFormat("MMM/yy");

        for(Date d : datas){
            yearMonth = s.format(d);
            if(!yearMonths.contains(yearMonth)){
                yearMonths.add(yearMonth);
            }
        }

        return yearMonths;
    }

    public static String standardString(String str) {
        String ret = "(vazio)";

        if(!str.isEmpty()){
            ret = firstCapital(deAccent(str)).trim();
        }

        return ret;
    }
    public static String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    public static String firstCapital(String name){
        String ret = "";

        if(!name.isEmpty()){
            ret = name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase();
        }
        return ret;
    }
}
