package com.melobarros.autocasher.fragment;


import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.EntryXComparator;
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

import java.lang.reflect.Array;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
@RequiresApi(api = Build.VERSION_CODES.O)
public class HistoricoFragment extends Fragment implements AdapterView.OnItemSelectedListener  {
    private static final String TAG = "HistoricoFragment";

    private List<Registro> registros = new ArrayList<>();
    private List<Abastecimento> abastecimentos = new ArrayList<>();
    private List<Gasto> gastos = new ArrayList<>();
    private List<Lembrete> lembretes = new ArrayList<>();
    private List<Manutencao> manutencoes = new ArrayList<>();
    Toolbar toolbar;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM");

    Retrofit retrofit;
    Retrofit retrofit_scalar;
    com.melobarros.autocasher.services.autocasherAPI autocasherAPI;
    com.melobarros.autocasher.services.autocasherAPI autocasherAPI_scalar;

    Gson gson = new Gson();

    private Spinner periodo_spinner;
    private static final String[] periodo_paths = {"Período", "15 dias", "30 dias", "90 dias", "1 ano", "2 anos", "5 anos"};
    String selectedSpinner;

    TextView abastecimento_qtde, manutencao_qtde, lembrete_qtde, gasto_qtde, totalDespesas, precoMedioLitro, consumoMedio, proximoLembrete;
    BarChart gastosBarChart, gastosTipoBarChart;
    PieChart despesasTipoChart;
    LineChart consumoMesChart;

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
        despesasTipoChart = view.findViewById(R.id.despesasTipo_pieChart);
        consumoMesChart = view.findViewById(R.id.consumoMes_lineChart);
        proximoLembrete = view.findViewById(R.id.proximoLembreteValor_textView);
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

            Calendar c = Calendar.getInstance();
            c.add(Calendar.YEAR, 1);
            c.getTimeInMillis();
            endDate = s.format(new Date(c.getTimeInMillis()));
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
        consumoMedio.setText(String.format("%.02f", getConsumoMedio(abastecimentos)) + " km/l");
        proximoLembrete.setText(getProximoLembrete(lembretes));


        if(!gastos.isEmpty()) {
            updateGastosMes();
            updateGastosTipo();
        }

        if(!abastecimentos.isEmpty()){
            updateConsumoMensal();
        }

        updateDespesasTipo();
    }

    private String getProximoLembrete(List<Lembrete> lembreteList){
        List<Lembrete> lembretesOrdenados = orderLembretesMaisAntigos(lembreteList);
        LocalDateTime today = LocalDateTime.now();

        for(Lembrete l : lembretesOrdenados){
            if(l.getLocalDateTime().isAfter(today)){
                return l.getLocalDateTime().format(formatter);
            }
        }

        return "-";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private float getConsumoMedio(List<Abastecimento> abs){
        float consumoMedio = 0.0f;
        float deltaOdometro;
        float consumoTemp = 0.0f;
        int contador = 0;
        List<Abastecimento> abastecimentosOrdenados = orderAbastecimentos(abs, "Mais novos");

        for(int i = 0; i < abastecimentosOrdenados.size() - 1; i++){
            Abastecimento b = abastecimentosOrdenados.get(i);
            Abastecimento bNext = abastecimentosOrdenados.get(i+1);
            if(b.isCompletandoTanque() && !b.isAbastecimentoAnteriorEmFalta() && b.getLitros() > 0 && b.getOdometro() > 0){
                if(b.getOdometro() - bNext.getOdometro() > 0){
                    deltaOdometro = b.getOdometro() - bNext.getOdometro();
                    consumoTemp = consumoTemp + (deltaOdometro / b.getLitros());
                    contador = contador + 1;
                }
            }
        }

        if(consumoTemp > 0.0f && contador > 0){
            consumoMedio = consumoTemp / contador;
        }

        return consumoMedio;
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
    private void updateConsumoMensal(){
        LineData lineData = new LineData(getConsumoMesDataSet());
        List<String> labels = getLabelsMes("Abastecimento");
        setupLineChart(consumoMesChart, lineData, labels);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateDespesasTipo(){
        List<Integer> colors = new ArrayList<Integer>();
        colors.add(Color.rgb(202,97,48));
        colors.add(Color.rgb(218,116,185));
        colors.add(Color.rgb(116,218,199));

        PieData pieData = new PieData(getDespesasTipoDataSet());
        List<String> labels = getDespesaLabels();
        setupPieChart(despesasTipoChart, pieData, labels, colors);
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
    private void setupLineChart(LineChart lineChart, LineData lineData, List<String> labels){
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setData(lineData);
        lineChart.setTouchEnabled(false);
        lineChart.animateXY(1500, 1500);
        XAxis bottomAxis = lineChart.getXAxis();
        bottomAxis.setLabelCount(labels.size());
        if(labels.size() >= 2){ bottomAxis.setLabelCount(labels.size(), true); }

        lineChart.invalidate();
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
        barChart.animateXY(1500, 1500);
        XAxis bottomAxis = barChart.getXAxis();
        bottomAxis.setLabelCount(labels.size());

        barChart.invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupPieChart(PieChart pieChart, PieData pieData, List<String> labels, List<Integer> colors){
        pieChart.getDescription().setEnabled(false);
        pieChart.setData(pieData);
        pieChart.setTouchEnabled(false);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieChart.setUsePercentValues(true);

        pieChart.animateY(1500);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);

        List<LegendEntry> entries = new ArrayList<>();

        for (int i = 0; i < labels.size(); i++) {
            LegendEntry entry = new LegendEntry();
            entry.formColor = colors.get(i);
            entry.label = labels.get(i);
            entries.add(entry);
        }

        legend.setCustom(entries);

        pieChart.invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<String> getDespesaLabels(){
        List<String> labels = new ArrayList<String>();

        labels.add("Abastecimento");
        labels.add("Gasto");
        labels.add("Manutenção");

        return labels;
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private PieDataSet getDespesasTipoDataSet(){
        List<PieEntry> pieEntries = new ArrayList<PieEntry>();
        List<String> labels = getDespesaLabels();
        List<Integer> colors = new ArrayList<Integer>();

        float abastecimentoSubtotal = 0.0f;
        float gastoSubtotal = 0.0f;
        float manutencaoSubtotal = 0.0f;
        float total = 0.0f;
        float abastecimentoPorcent = 0.0f;
        float gastoPorcent = 0.0f;
        float manutencaoPorcent = 0.0f;

        for(Abastecimento a : abastecimentos){ abastecimentoSubtotal = abastecimentoSubtotal + (a.getLitros() * a.getPrecoLitro()); }
        for(Gasto g : gastos){ gastoSubtotal = gastoSubtotal + g.getValorTotal(); }
        for(Manutencao m : manutencoes){ manutencaoSubtotal = manutencaoSubtotal + m.getValor(); }

        colors.add(Color.rgb(202,97,48));
        colors.add(Color.rgb(218,116,185));
        colors.add(Color.rgb(116,218,199));

        total = abastecimentoSubtotal + gastoSubtotal + manutencaoSubtotal;

        if(total > 0){
            abastecimentoPorcent = (abastecimentoSubtotal / total)*100;
            gastoPorcent = (gastoSubtotal / total)*100;
            manutencaoPorcent = (manutencaoSubtotal / total)*100;
        }

        pieEntries.add(labels.indexOf("Abastecimento"), new PieEntry(abastecimentoPorcent));
        pieEntries.add(labels.indexOf("Gasto"), new PieEntry(gastoPorcent));
        pieEntries.add(labels.indexOf("Manutenção"), new PieEntry(manutencaoPorcent));

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Despesas");
        pieDataSet.setHighlightEnabled(false);
        pieDataSet.setColors(colors);
        pieDataSet.setValueTextSize(11f);

        return pieDataSet;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private LineDataSet getConsumoMesDataSet(){
        List<Entry> lineEntries = new ArrayList<Entry>();
        List<String> labels = getLabelsMes("Abastecimento");
        Map<String, Float> consumoMap = getConsumoYearMonthTotalValueMap(labels);

        for(Map.Entry<String, Float> entry : consumoMap.entrySet()){
            String tipo = entry.getKey();
            Float valorTotal = entry.getValue();
            lineEntries.add(new BarEntry(labels.indexOf(tipo), valorTotal));
            Log.d(TAG, "##### Mes: " + tipo + " #### Consumo: " + valorTotal + " #### ListIndex: " + labels.indexOf(tipo));
        }

        Collections.sort(lineEntries, new EntryXComparator());

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Consumo");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setColor(Color.rgb(21,48,97));
        lineDataSet.setValueTextColor(Color.rgb(64,6,6));
        lineDataSet.setValueTextSize(11f);

        lineDataSet.setHighlightEnabled(false);

        return lineDataSet;
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
    private Map<String, Float> getConsumoYearMonthTotalValueMap(List<String> labels){
        Map<String, Float> consumoYearMonthTotalValueMap = new HashMap<String, Float>();
        String yearMonth;
        SimpleDateFormat s = new SimpleDateFormat("MMM/yy");
        List<Abastecimento> abastecimentosTemp = new ArrayList<Abastecimento>();
        Float consumoMedio;

        for(String l : labels){
            consumoYearMonthTotalValueMap.put(l, 0.0f);
        }

        for(String label : labels){
            for(Abastecimento a : abastecimentos){
                yearMonth = s.format(Date.from(a.getLocalDateTime().atZone(ZoneId.systemDefault()).toInstant()));
                if(label.equals(yearMonth)){
                    abastecimentosTemp.add(a);
                }
            }

            consumoMedio = getConsumoMedio(abastecimentosTemp);
            if(consumoMedio > 0.0f){
                consumoYearMonthTotalValueMap.put(label, consumoYearMonthTotalValueMap.get(label) + consumoMedio);
            }
        }

        return consumoYearMonthTotalValueMap;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<Abastecimento> orderAbastecimentos(List<Abastecimento> abastecimentos, String selectedOrder){
        List<Abastecimento> list = abastecimentos;

        switch (selectedOrder){
            case "Ordernar por":
            case "Mais novos":
                Collections.sort(list, (x, y) -> x.getLocalDateTime().compareTo(y.getLocalDateTime()));
                Collections.reverse(list);
                break;
            case "Mais antigos":
                Collections.sort(list, (x, y) -> x.getLocalDateTime().compareTo(y.getLocalDateTime()));
                break;
            case "Maior valor":
                Collections.sort(list, new Comparator<Abastecimento>() {
                    @Override
                    public int compare(Abastecimento o1, Abastecimento o2) {
                        return Float.compare(o1.getLitros() * o1.getPrecoLitro(), o2.getLitros() * o2.getPrecoLitro());
                    }
                });

                Collections.reverse(list);
                break;
            case "Menor valor":
                Collections.sort(list, new Comparator<Abastecimento>() {
                    @Override
                    public int compare(Abastecimento o1, Abastecimento o2) {
                        return Float.compare(o1.getLitros() * o1.getPrecoLitro(), o2.getLitros() * o2.getPrecoLitro());
                    }
                });
                break;
        }

        return list;
    }

    public List<Lembrete> orderLembretesMaisAntigos(List<Lembrete> _lembretes){
        List<Lembrete> list = lembretes;
        Collections.sort(list, (x, y) -> x.getLocalDateTime().compareTo(y.getLocalDateTime()));

        return list;
    }
}
