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
import com.melobarros.autocasher.model.Abastecimento;
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
public class EditarAbastecimentoActivity extends AppCompatActivity implements DatePickerFragment.TheListener  {
    private static final String TAG = "EditarAbastActivity";

    public TextInputEditText quantidadeLitrosAbastecimento, valorLitroAbastecimento, dataAbastecimento, infoAdicionalAbastecimento, odometroAbastecimento;
    public Button btnSalvar, btnDescartar;
    public ImageButton btnDataPicker;

    Toolbar toolbar;
    Retrofit retrofit;
    autocasherAPI autocasherAPI;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_abastecimento);

        toolbar = findViewById(R.id.EditarAbastecimento_toolbar);

        initService();
        initToolbar();
        initComponentes();

        Abastecimento abastecimento = (Abastecimento)getIntent().getSerializableExtra("Abastecimento");
        final Abastecimento a = abastecimento;

        if(abastecimento != null){
            setTexts(abastecimento);
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

                if(a != null){
                    updateAbastecimento(EditarAbastecimentoActivity.this);
                } else{
                    createAbastecimento(EditarAbastecimentoActivity.this);
                }


            }
        });
    }

    private void createAbastecimento(final Context c){
        final Abastecimento a = new Abastecimento();
        LocalDateTime dt = LocalDate.parse(dataAbastecimento.getText().toString(), formatter).atStartOfDay();

        a.setObservacao(infoAdicionalAbastecimento.getText().toString());
        a.setPrecoLitro(Float.valueOf(valorLitroAbastecimento.getText().toString()));
        a.setDateTime(dt.toString());
        a.setLitros(Float.valueOf(quantidadeLitrosAbastecimento.getText().toString()));
        a.setOdometro(Float.valueOf(odometroAbastecimento.getText().toString()));
        a.setTipo("abastecimento");

        Call<Abastecimento> requestInsert = autocasherAPI.insertAbastecimento(a);

        requestInsert.enqueue(new Callback<Abastecimento>() {
            @Override
            public void onResponse(Call<Abastecimento> call, Response<Abastecimento> response) {
                if(!response.isSuccessful()){
                    Log.e(TAG, "Erro: " + response.code());
                    finish();
                    return;
                } else{
                    if (response.body().getId() > 0) {
                        Toast.makeText(c, "ABASTECIMENTO INSERIDO COM SUCESSO",Toast.LENGTH_SHORT).show();
                        finish();
                    } else{
                        Toast.makeText(c, "FALHA AO INSERIR ABASTECIMENTO",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<Abastecimento> call, Throwable t) {
                Log.e(TAG, "Erro Failure: " + t.getMessage());
            }
        });
    }

    private void updateAbastecimento(final Context c){
        final Abastecimento a = (Abastecimento) getIntent().getSerializableExtra("Abastecimento");
        LocalDateTime dt = LocalDate.parse(dataAbastecimento.getText().toString(), formatter).atStartOfDay();

        a.setObservacao(infoAdicionalAbastecimento.getText().toString());
        a.setPrecoLitro(Float.valueOf(valorLitroAbastecimento.getText().toString()));
        a.setDateTime(dt.toString());
        a.setLitros(Float.valueOf(quantidadeLitrosAbastecimento.getText().toString()));
        a.setOdometro(Float.valueOf(odometroAbastecimento.getText().toString()));
        a.setTipo("abastecimento");

        Call<Abastecimento> requestUpdate = autocasherAPI.updateAbastecimento(a);

        requestUpdate.enqueue(new Callback<Abastecimento>() {
            @Override
            public void onResponse(Call<Abastecimento> call, Response<Abastecimento> response) {
                if(!response.isSuccessful()){
                    Log.e(TAG, "Erro: " + response.code());
                    return;
                } else{
                    if (a.getId() == response.body().getId()) {
                        Toast.makeText(c, "ABASTECIMENTO ATUALIZADO COM SUCESSO",Toast.LENGTH_SHORT).show();
                        finish();
                    } else{
                        Toast.makeText(c, "FALHA AO ATUALIZAR ABASTECIMENTO",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<Abastecimento> call, Throwable t) {
                Log.e(TAG, "Erro Failure: " + t.getMessage());
            }
        });
    }

    public void setTexts(Abastecimento abastecimento){
        quantidadeLitrosAbastecimento.setText(String.valueOf(abastecimento.getLitros()));
        valorLitroAbastecimento.setText(String.valueOf(abastecimento.getPrecoLitro()));
        dataAbastecimento.setText(abastecimento.getLocalDateTime().format(formatter));
        infoAdicionalAbastecimento.setText(abastecimento.getObservacao());
        odometroAbastecimento.setText(String.format("%.0f", abastecimento.getOdometro()));
    }

    public void setInitNumbers(){
        quantidadeLitrosAbastecimento.setText("0.0");
        valorLitroAbastecimento.setText("0.0");
        odometroAbastecimento.setText("0.0");
        dataAbastecimento.setText(LocalDateTime.now().format(formatter));
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
        toolbar.setTitle("Editar Abastecimento");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
    }

    public void initComponentes(){
        quantidadeLitrosAbastecimento = findViewById(R.id.quantidadeLitros_input);
        valorLitroAbastecimento = findViewById(R.id.valorAbastecimento_input);
        dataAbastecimento = findViewById(R.id.dataAbastecimento_input);
        dataAbastecimento.setFocusable(false);
        infoAdicionalAbastecimento = findViewById(R.id.infoAdicionalAbastecimento_input);
        odometroAbastecimento = findViewById(R.id.odometroAbastecimento_input);
        btnDescartar = findViewById(R.id.descartarAbastecimento_button);
        btnSalvar = findViewById(R.id.salvarAbastecimento_button);
        btnDataPicker = findViewById(R.id.dataPicker_Abastecimento_imageButton);
    }

    @Override
    public void returnDate(String date) {
        dataAbastecimento.setText(date);
    }
}
