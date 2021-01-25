package com.melobarros.autocasher.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Toast;


import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.melobarros.autocasher.R;
import com.melobarros.autocasher.fragment.DatePickerFragment;
import com.melobarros.autocasher.model.Gasto;
import com.melobarros.autocasher.services.autocasherAPI;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@RequiresApi(api = Build.VERSION_CODES.O)
public class EditarGastoActivity extends AppCompatActivity implements DatePickerFragment.TheListener{
    private static final String TAG = "EditarGastoActivity";

    public TextInputEditText tipoGasto, valorGasto, dataGasto, localGasto, infoAdicionalGasto, odometroGasto;
    public Button btnSalvar, btnDescartar;
    public ImageButton btnDataPicker;

    Toolbar toolbar;
    Retrofit retrofit;
    autocasherAPI autocasherAPI;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy");


    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_gasto);
        toolbar = findViewById(R.id.EditarGasto_toolbar);

        initService();
        initToolbar();
        initComponentes();

        Gasto gasto = (Gasto)getIntent().getSerializableExtra("Gasto");
        final Gasto g = gasto;

        if(gasto != null){
            setTexts(gasto);
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

                if(g != null){
                    updateGasto(EditarGastoActivity.this);
                } else{
                    createGasto(EditarGastoActivity.this);
                }


            }
        });
    }

    public void setTexts(Gasto gasto){
        tipoGasto.setText(gasto.getObservacao());
        valorGasto.setText(String.valueOf(gasto.getValorTotal()));
        dataGasto.setText(gasto.getLocalDateTime().format(formatter));
        localGasto.setText(gasto.getLocal());
        infoAdicionalGasto.setText(gasto.getMotivo());
        odometroGasto.setText(String.valueOf(gasto.getOdometro()));
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
        toolbar.setTitle("Editar Gasto");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
    }

    public void setInitNumbers(){
        valorGasto.setText("0.0");
        dataGasto.setText(LocalDateTime.now().format(formatter));
        odometroGasto.setText("0.0");
    }

    public void initComponentes(){
        tipoGasto = findViewById(R.id.tipoGasto_input);
        valorGasto = findViewById(R.id.valorGasto_input);
        dataGasto = findViewById(R.id.dataGasto_input);
        dataGasto.setFocusable(false);
        localGasto = findViewById(R.id.localGasto_input);
        infoAdicionalGasto = findViewById(R.id.infoAdicionalGasto_input);
        odometroGasto = findViewById(R.id.odometro_input);
        btnDescartar = findViewById(R.id.descartarGasto_button);
        btnSalvar = findViewById(R.id.salvarGasto_button);
        btnDataPicker = findViewById(R.id.dataPicker_imageButton);
    }

    @Override
    public void returnDate(String date) {
        dataGasto.setText(date);
    }

    private void createGasto(final Context c){
        final Gasto g = new Gasto();
        LocalDateTime dt = LocalDate.parse(dataGasto.getText().toString(), formatter).atStartOfDay();

        g.setObservacao(tipoGasto.getText().toString());
        g.setValorTotal(Float.valueOf(valorGasto.getText().toString()));
        g.setDateTime(dt.toString());
        g.setLocal(localGasto.getText().toString());
        g.setMotivo(infoAdicionalGasto.getText().toString());
        g.setOdometro(Float.valueOf(odometroGasto.getText().toString()));
        g.setTipo("gasto");

        Call<Gasto> requestInsert = autocasherAPI.insertGasto(g);

        requestInsert.enqueue(new Callback<Gasto>() {
            @Override
            public void onResponse(Call<Gasto> call, Response<Gasto> response) {
                if(!response.isSuccessful()){
                    Log.e(TAG, "Erro: " + response.code());
                    finish();
                    return;
                } else{
                    if (response.body().getId() > 0) {
                        Toast.makeText(c, "GASTO INSERIDO COM SUCESSO",Toast.LENGTH_SHORT).show();
                        finish();
                    } else{
                        Toast.makeText(c, "FALHA AO INSERIR GASTO",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<Gasto> call, Throwable t) {
                Log.e(TAG, "Erro Failure: " + t.getMessage());
            }
        });
    }

    private void updateGasto(final Context c){
        final Gasto g = (Gasto)getIntent().getSerializableExtra("Gasto");
        LocalDateTime dt = LocalDate.parse(dataGasto.getText().toString(), formatter).atStartOfDay();

        g.setObservacao(tipoGasto.getText().toString());
        g.setValorTotal(Float.valueOf(valorGasto.getText().toString()));
        g.setDateTime(dt.toString());
        g.setLocal(localGasto.getText().toString());
        g.setMotivo(infoAdicionalGasto.getText().toString());
        g.setOdometro(Float.valueOf(odometroGasto.getText().toString()));
        g.setTipo("gasto");

        Call<Gasto> requestUpdate = autocasherAPI.updateGasto(g);

        requestUpdate.enqueue(new Callback<Gasto>() {
            @Override
            public void onResponse(Call<Gasto> call, Response<Gasto> response) {
                if(!response.isSuccessful()){
                    Log.e(TAG, "Erro: " + response.code());
                    return;
                } else{
                    if (g.getId() == response.body().getId()) {
                        Toast.makeText(c, "GASTO ATUALIZADO COM SUCESSO",Toast.LENGTH_SHORT).show();
                        finish();
                    } else{
                        Toast.makeText(c, "FALHA AO ATUALIZAR GASTO",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<Gasto> call, Throwable t) {
                Log.e(TAG, "Erro Failure: " + t.getMessage());
            }
        });
    }
}
