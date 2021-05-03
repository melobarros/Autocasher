package com.melobarros.autocasher.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.melobarros.autocasher.MainActivity;
import com.melobarros.autocasher.R;
import com.melobarros.autocasher.model.Abastecimento;
import com.melobarros.autocasher.services.autocasherAPI;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    Retrofit retrofit;
    com.melobarros.autocasher.services.autocasherAPI autocasherAPI;

    Button startButton;
    TextView login_textView;
    ProgressBar login_progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        startButton = findViewById(R.id.start_button);
        login_textView = findViewById(R.id.login_textView);
        login_progressBar = findViewById(R.id.login_progressBar);

        login_progressBar.setVisibility(View.INVISIBLE);
        login_textView.setVisibility(View.INVISIBLE);

        initService();
        Log.d(TAG, "onCreateView: started.");

        testGooglePost();

        startButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                login_progressBar.setVisibility(View.VISIBLE);
                login_textView.setVisibility(View.VISIBLE);

                initAbastecimentos(v);
            }
        });

    }

    private void testGooglePost(){
        try {
            OkHttpClient client = new OkHttpClient();
            FormBody body = new FormBody.Builder()
                    .add( "entry.1656527820", "2" )
                    .add( "entry.396537152", "Manutenção anual" )
                    .add( "entry.424100007", "R$ 500,00" )
                    .add( "entry.1516873583", "04/05/2021" )
                    .add( "entry.1930651737", "VW Barigui" )
                    .add( "entry.2077132507", "Escolher a básica" )
                    .add( "entry.1165123994", "CREATE" )
                    .build();
            Request request = new Request.Builder()
                    .url( "https://docs.google.com/forms/d/e/1FAIpQLSesVzR68prhsHdaJa_eIWzV2dZlgyqsiZN1pGRqdzY-o19hiA/formResponse" )
                    .post( body )
                    .build();
            //okhttp3.Response response = client.newCall( request ).execute();
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

    private void initAbastecimentos(View v){
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
                    Log.d(TAG, "Response arrived!");
                    login_progressBar.setVisibility(View.INVISIBLE);
                    login_textView.setVisibility(View.INVISIBLE);
                    Intent i = new Intent(v.getContext(), MainActivity.class);
                    v.getContext().startActivity(i);
                }
            }

            @Override
            public void onFailure(Call<List<Abastecimento>> call, Throwable t) {
                Log.e(TAG, "Erro Failure: " + t.getMessage());
            }
        });
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
        final OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(180, TimeUnit.SECONDS)
                .connectTimeout(180, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();


        retrofit = new Retrofit.Builder()
                .baseUrl(autocasherAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        autocasherAPI = retrofit.create(com.melobarros.autocasher.services.autocasherAPI.class);
    }
}