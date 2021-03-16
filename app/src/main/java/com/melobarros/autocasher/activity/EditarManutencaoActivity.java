package com.melobarros.autocasher.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.melobarros.autocasher.R;
import com.melobarros.autocasher.fragment.DatePickerFragment;
import com.melobarros.autocasher.model.Gasto;
import com.melobarros.autocasher.model.Manutencao;
import com.melobarros.autocasher.services.autocasherAPI;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@RequiresApi(api = Build.VERSION_CODES.O)
public class EditarManutencaoActivity extends AppCompatActivity implements DatePickerFragment.TheListener {
    private static final String TAG = "EditarManutActivity";

    public TextInputEditText tipoManutencao, valorManutencao, dataManutencao, localManutencao, infoAdicionalManutencao, pecasManutencao;
    public Button btnSalvar, btnDescartar;
    public ImageButton btnDataPicker;

    Toolbar toolbar;
    Retrofit retrofit;
    autocasherAPI autocasherAPI;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy");
    Float tempValor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_manutencao);
        toolbar = findViewById(R.id.EditarManutencao_toolbar);

        initService();
        initToolbar();
        initComponentes();

        Manutencao manutencao = (Manutencao)getIntent().getSerializableExtra("Manutencao");
        final Manutencao m = manutencao;

        if(manutencao != null){
            setTexts(manutencao);
        } else{
            setInitNumbers();
        }

        btnDataPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment picker = new DatePickerFragment();
                picker.show(getSupportFragmentManager(), "datePicker");
            }
        });

        btnDescartar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(m != null){
                    updateManutencao(EditarManutencaoActivity.this);
                } else{
                    createManutencao(EditarManutencaoActivity.this);
                }


            }
        });
    }

    private Float verificaNuloFloat(String val){
        return val.trim().length() > 0 ? Float.valueOf(val.replace(',','.')) : 0;
    }

    private void createManutencao(final Context c){
        final Manutencao m = new Manutencao();
        LocalDateTime dt = LocalDate.parse(dataManutencao.getText().toString(), formatter).atStartOfDay();

        tempValor = verificaNuloFloat(valorManutencao.getText().toString());

        m.setObservacao(tipoManutencao.getText().toString());
        m.setValor(tempValor);
        m.setDateTime(dt.toString());
        m.setLocal(localManutencao.getText().toString());
        m.setDescricao(infoAdicionalManutencao.getText().toString());
        m.setPecas(pecasManutencao.getText().toString());
        m.setTipo("manutencao");

        Call<Manutencao> requestInsert = autocasherAPI.insertManutencao(m);

        requestInsert.enqueue(new Callback<Manutencao>() {
            @Override
            public void onResponse(Call<Manutencao> call, Response<Manutencao> response) {
                if(!response.isSuccessful()){
                    Log.e(TAG, "Erro: " + response.code());
                    finish();
                    return;
                } else{
                    if (response.body().getId() > 0) {
                        Toast.makeText(c, "MANUTENCAO INSERIDA COM SUCESSO",Toast.LENGTH_SHORT).show();
                        finish();
                    } else{
                        Toast.makeText(c, "FALHA AO INSERIR MANUTENCAO",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<Manutencao> call, Throwable t) {
                Log.e(TAG, "Erro Failure: " + t.getMessage());
            }
        });
    }

    private void updateManutencao(final Context c){
        final Manutencao m = (Manutencao) getIntent().getSerializableExtra("Manutencao");
        LocalDateTime dt = LocalDate.parse(dataManutencao.getText().toString(), formatter).atStartOfDay();

        tempValor = verificaNuloFloat(valorManutencao.getText().toString());

        m.setObservacao(tipoManutencao.getText().toString());
        m.setValor(tempValor);
        m.setDateTime(dt.toString());
        m.setLocal(localManutencao.getText().toString());
        m.setDescricao(infoAdicionalManutencao.getText().toString());
        m.setPecas(pecasManutencao.getText().toString());
        m.setTipo("manutencao");

        Call<Manutencao> requestUpdate = autocasherAPI.updateManutencao(m);

        requestUpdate.enqueue(new Callback<Manutencao>() {
            @Override
            public void onResponse(Call<Manutencao> call, Response<Manutencao> response) {
                if(!response.isSuccessful()){
                    Log.e(TAG, "Erro: " + response.code());
                    return;
                } else{
                    if (m.getId() == response.body().getId()) {
                        Toast.makeText(c, "MANUTENCAO ATUALIZADA COM SUCESSO",Toast.LENGTH_SHORT).show();
                        finish();
                    } else{
                        Toast.makeText(c, "FALHA AO ATUALIZAR MANUTENCAO",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<Manutencao> call, Throwable t) {
                Log.e(TAG, "Erro Failure: " + t.getMessage());
            }
        });
    }

    @Override
    public void returnDate(String date) {
        dataManutencao.setText(date);
    }

    public void setTexts(Manutencao manutencao){
        tipoManutencao.setText(manutencao.getObservacao());
        valorManutencao.setText(String.valueOf(manutencao.getValor()));
        dataManutencao.setText(manutencao.getLocalDateTime().format(formatter));
        localManutencao.setText(manutencao.getLocal());
        infoAdicionalManutencao.setText(manutencao.getDescricao());
        pecasManutencao.setText(String.valueOf(manutencao.getPecas()));
    }

    public void setInitNumbers(){
        valorManutencao.setText("0.0");
        dataManutencao.setText(LocalDateTime.now().format(formatter));
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
        toolbar.setTitle("Editar Manutencao");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
    }

    public void initComponentes(){
        tipoManutencao = findViewById(R.id.tipoManutencao_input);
        valorManutencao = findViewById(R.id.valorManutencao_input);
        dataManutencao = findViewById(R.id.dataManutencao_input);
        dataManutencao.setFocusable(false);
        localManutencao = findViewById(R.id.localManutencao_input);
        infoAdicionalManutencao = findViewById(R.id.infoAdicionalManutencao_input);
        pecasManutencao = findViewById(R.id.pecasManutencao_input);
        btnDescartar = findViewById(R.id.descartarManutencao_button);
        btnSalvar = findViewById(R.id.salvarManutencao_button);
        btnDataPicker = findViewById(R.id.dataPicker_Manutencao_imageButton);
    }
}
