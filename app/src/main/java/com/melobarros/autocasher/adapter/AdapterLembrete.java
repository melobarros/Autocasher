package com.melobarros.autocasher.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.melobarros.autocasher.R;
import com.melobarros.autocasher.activity.EditarLembreteActivity;
import com.melobarros.autocasher.activity.EditarManutencaoActivity;
import com.melobarros.autocasher.model.Lembrete;
import com.melobarros.autocasher.model.Manutencao;
import com.melobarros.autocasher.services.autocasherAPI;

import java.time.format.DateTimeFormatter;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AdapterLembrete extends RecyclerView.Adapter<AdapterLembrete.LembreteViewHolder>  {
    private static final String TAG = "AdapterLembrete";

    private List<Lembrete> lembreteList;
    private Context context;
    Retrofit retrofit;
    autocasherAPI autocasherAPI;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy");

    public AdapterLembrete(List<Lembrete> l, Context c){
        this.lembreteList = l;
        this.context = c;
    }

    @NonNull
    @Override
    public LembreteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_lembrete, parent, false);
        initService();

        return new AdapterLembrete.LembreteViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull LembreteViewHolder holder, int position) {
        final Lembrete lembrete = lembreteList.get(position);

        holder.tipo.setText(lembrete.getTipo());
        holder.dateTime.setText(lembrete.getLocalDateTime().format(formatter));
        holder.observacao.setText(lembrete.getObservacao());
        holder.valorPrevisto.setText("R$ " + String.format("%.02f", lembrete.getValorPrevisto()));
        holder.descricao.setText(lembrete.getDescricao());
        holder.repetirCada.setText("Repetir a cada " + String.valueOf(lembrete.getRepetirCada()) + " meses");
        holder.local.setText(lembrete.getLocal());
        holder.subItem.setVisibility(lembrete.isExpanded() ? View.VISIBLE : View.GONE);

        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Lembrete l = lembrete;
                l.setTipo("lembrete");

                Call<Void> requestDelete = autocasherAPI.deleteLembrete(l);

                requestDelete.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if(!response.isSuccessful()){
                            Log.e(TAG, "Erro: " + response.code());
                            return;
                        } else{
                            Toast.makeText(context, "LEMBRETE REMOVIDO COM SUCESSO",Toast.LENGTH_SHORT).show();
                            lembreteList.remove(position);
                            notifyItemRemoved(position);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "Erro Failure: " + t.getMessage());
                    }
                });
            }
        });

        holder.btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), EditarLembreteActivity.class);
                i.putExtra("Lembrete", lembrete);
                v.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lembreteList.size();
    }

    public class LembreteViewHolder extends RecyclerView.ViewHolder {
        TextView descricao;
        TextView repetirCada;
        TextView dateTime;
        TextView valorPrevisto;
        TextView tipo;
        TextView local;
        TextView observacao;
        LinearLayout subItem;
        LinearLayout lembreteCard;
        ImageButton btn_edit;
        ImageButton btn_delete;


        public LembreteViewHolder(@NonNull View itemView) {
            super(itemView);

            descricao = itemView.findViewById(R.id.lembrete_descricao_textView);
            repetirCada = itemView.findViewById(R.id.lembrete_repeatEvery_textView);
            dateTime = itemView.findViewById(R.id.lembrete_data_textView);
            valorPrevisto = itemView.findViewById(R.id.lembrete_valorPrevisto_textView);
            tipo = itemView.findViewById(R.id.lembrete_tipoRegistro_textView);
            local = itemView.findViewById(R.id.lembrete_local_textView);
            observacao = itemView.findViewById(R.id.lembrete_observacao_textView);
            subItem = itemView.findViewById(R.id.lembrete_sub_item);
            lembreteCard = itemView.findViewById(R.id.lembreteCard);
            btn_edit = itemView.findViewById(R.id.lembrete_edit_imageView);
            btn_delete = itemView.findViewById(R.id.lembrete_delete_imageView);

            lembreteCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Lembrete lembrete = lembreteList.get(getAdapterPosition());
                    lembrete.setExpanded(!lembrete.isExpanded());
                    notifyItemChanged(getAdapterPosition());
                }
            });
        }
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
}
