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
import com.melobarros.autocasher.model.Lembrete;
import com.melobarros.autocasher.model.Manutencao;
import com.melobarros.autocasher.services.autocasherAPI;
import com.melobarros.autocasher.utils.GoogleFormLembrete;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@RequiresApi(api = Build.VERSION_CODES.O)
public class EditarLembreteActivity extends AppCompatActivity implements DatePickerFragment.TheListener {

    private static final String TAG = "EditarLembreteActivity";

    public TextInputEditText descricaoLembrete, valorLembrete, dataLembrete, localLembrete, observacaoLembrete;
    public Button btnSalvar, btnDescartar;
    public ImageButton btnDataPicker;

    Toolbar toolbar;
    Retrofit retrofit;
    autocasherAPI autocasherAPI;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy");
    DateTimeFormatter formatterShort = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    Integer tempRepetirCada;
    Float tempValorPrevisto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_lembrete);
        toolbar = findViewById(R.id.EditarLembrete_toolbar);

        initService();
        initToolbar();
        initComponentes();

        Lembrete lembrete = (Lembrete)getIntent().getSerializableExtra("Lembrete");
        final Lembrete l = lembrete;

        if(lembrete != null){
            setTexts(lembrete);
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

                if(l != null){
                    updateLembrete(EditarLembreteActivity.this);
                } else{
                    createLembrete(EditarLembreteActivity.this);
                }


            }
        });
    }

    @Override
    public void returnDate(String date) {
        dataLembrete.setText(date);
    }

    private GoogleFormLembrete setupFormLembrete(Lembrete lembrete, String acao){
        GoogleFormLembrete form = new GoogleFormLembrete();
        form.setId(String.valueOf(lembrete.getId()));
        form.setDescricao(lembrete.getDescricao());
        form.setValorPrevisto("R$ " + String.format("%.02f", lembrete.getValorPrevisto()));
        form.setDataLembrete(lembrete.getLocalDateTime().format(formatterShort));
        form.setLocal(lembrete.getLocal());
        form.setObservacao(lembrete.getObservacao());
        form.setAcao(acao);

        return form;
    }

    private void postFormLembrete(GoogleFormLembrete form){
        try {
            OkHttpClient client = new OkHttpClient();
            FormBody body = new FormBody.Builder()
                    .add( "entry.1656527820", form.getId() )
                    .add( "entry.396537152", form.getDescricao() )
                    .add( "entry.424100007", form.getValorPrevisto() )
                    .add( "entry.1516873583", form.getDataLembrete() )
                    .add( "entry.1930651737", form.getLocal() )
                    .add( "entry.2077132507", form.getObservacao() )
                    .add( "entry.1165123994", form.getAcao() )
                    .build();
            Request request = new Request.Builder()
                    .url( "https://docs.google.com/forms/d/e/1FAIpQLSesVzR68prhsHdaJa_eIWzV2dZlgyqsiZN1pGRqdzY-o19hiA/formResponse" )
                    .post( body )
                    .build();
            client.newCall( request ).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                    Log.v(TAG, "Erro Google Form Post: " + e.getMessage());
                }

                @Override
                public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) throws IOException {
                    if(!response.isSuccessful()){
                        Log.v(TAG, "Erro ao postar no Google Form! [" + response.toString() + "]");
                    } else{
                        Log.v(TAG, "Sucesso ao postar no Google Form!");
                    }
                }
            });


        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    private void createLembrete(final Context c){
        final Lembrete l = new Lembrete();
        LocalDateTime dt = LocalDate.parse(dataLembrete.getText().toString(), formatter).atStartOfDay();

        tempValorPrevisto = verificaNuloFloat(valorLembrete.getText().toString());

        l.setDescricao(descricaoLembrete.getText().toString());
        l.setValorPrevisto(tempValorPrevisto);
        l.setDateTime(dt.toString());
        l.setLocal(localLembrete.getText().toString());
        l.setObservacao(observacaoLembrete.getText().toString());
        l.setRepetirCada(0);
        l.setTipo("lembrete");

        Call<Lembrete> requestInsert = autocasherAPI.insertLembrete(l);

        requestInsert.enqueue(new Callback<Lembrete>() {
            @Override
            public void onResponse(Call<Lembrete> call, Response<Lembrete> response) {
                if(!response.isSuccessful()){
                    Log.e(TAG, "Erro: " + response.code());
                    finish();
                    return;
                } else{
                    if (response.body().getId() > 0) {
                        GoogleFormLembrete form = setupFormLembrete(response.body(), "CREATE");
                        postFormLembrete(form);

                        Toast.makeText(c, "LEMBRETE INSERIDO COM SUCESSO",Toast.LENGTH_SHORT).show();
                        finish();
                    } else{
                        Toast.makeText(c, "FALHA AO INSERIR LEMBRETE",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<Lembrete> call, Throwable t) {
                Log.e(TAG, "Erro Failure: " + t.getMessage());
            }
        });
    }

    private void updateLembrete(final Context c){
        final Lembrete l = (Lembrete) getIntent().getSerializableExtra("Lembrete");
        LocalDateTime dt = LocalDate.parse(dataLembrete.getText().toString(), formatter).atStartOfDay();

        tempValorPrevisto = verificaNuloFloat(valorLembrete.getText().toString());

        l.setDescricao(descricaoLembrete.getText().toString());
        l.setValorPrevisto(tempValorPrevisto);
        l.setDateTime(dt.toString());
        l.setLocal(localLembrete.getText().toString());
        l.setObservacao(observacaoLembrete.getText().toString());
        l.setRepetirCada(0);
        l.setTipo("lembrete");

        Call<Lembrete> requestUpdate = autocasherAPI.updateLembrete(l);

        requestUpdate.enqueue(new Callback<Lembrete>() {
            @Override
            public void onResponse(Call<Lembrete> call, Response<Lembrete> response) {
                if(!response.isSuccessful()){
                    Log.e(TAG, "Erro: " + response.code());
                    return;
                } else{
                    if (l.getId() == response.body().getId()) {
                        GoogleFormLembrete form = setupFormLembrete(response.body(), "UPDATE");
                        postFormLembrete(form);

                        Toast.makeText(c, "LEMBRETE ATUALIZADO COM SUCESSO",Toast.LENGTH_SHORT).show();
                        finish();
                    } else{
                        Toast.makeText(c, "FALHA AO ATUALIZAR LEMBRETE",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<Lembrete> call, Throwable t) {
                Log.e(TAG, "Erro Failure: " + t.getMessage());
            }
        });
    }

    private Integer verificaNulo(String val){
        return val.trim().length() > 0 ? Integer.valueOf(val) : 0;
    }

    private Float verificaNuloFloat(String val){
        return val.trim().length() > 0 ? Float.valueOf(val.replace(',','.')) : 0;
    }

    public void setTexts(Lembrete lembrete){
        descricaoLembrete.setText(lembrete.getDescricao());
        valorLembrete.setText(String.valueOf(lembrete.getValorPrevisto()));
        dataLembrete.setText(lembrete.getLocalDateTime().format(formatter));
        localLembrete.setText(lembrete.getLocal());
        observacaoLembrete.setText(lembrete.getObservacao());
        //repetirCadaLembrete.setText(String.valueOf(lembrete.getRepetirCada()));
    }

    public void setInitNumbers(){
        valorLembrete.setText("0.0");
        dataLembrete.setText(LocalDateTime.now().format(formatter));
        //repetirCadaLembrete.setText("0");
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
        toolbar.setTitle("Editar Lembrete");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
    }

    public void initComponentes(){
        descricaoLembrete = findViewById(R.id.descricaoLembrete_input);
        valorLembrete = findViewById(R.id.valorLembrete_input);
        dataLembrete = findViewById(R.id.dataLembrete_input);
        dataLembrete.setFocusable(false);
        localLembrete = findViewById(R.id.localLembrete_input);
        //repetirCadaLembrete = findViewById(R.id.repetirCadaLembrete_input);
        observacaoLembrete = findViewById(R.id.observacaoLembrete_input);
        btnDescartar = findViewById(R.id.descartarLembrete_button);
        btnSalvar = findViewById(R.id.salvarLembrete_button);
        btnDataPicker = findViewById(R.id.dataPicker_Lembrete_imageButton);
    }
}
