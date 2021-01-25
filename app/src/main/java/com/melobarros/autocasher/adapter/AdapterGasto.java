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
import com.melobarros.autocasher.activity.EditarGastoActivity;
import com.melobarros.autocasher.model.Gasto;
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
public class AdapterGasto extends RecyclerView.Adapter<AdapterGasto.GastoViewHolder> {
    private static final String TAG = "AdapterGastoActivity";

    private List<Gasto> listaGasto;
    private Context context;

    Retrofit retrofit;
    autocasherAPI autocasherAPI;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy");

    public AdapterGasto(List<Gasto> l, Context c) {
        this.listaGasto = l;
        this.context = c;
    }

    @NonNull
    @Override
    public GastoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_gasto, parent, false);
        initService();

        return new GastoViewHolder(itemLista);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull GastoViewHolder holder, final int position) {
        final Gasto gasto = listaGasto.get(position);

        holder.tipo.setText(gasto.getTipo());
        holder.dateTime.setText(gasto.getLocalDateTime().format(formatter));
        holder.observacao.setText(gasto.getObservacao());
        holder.valorTotal.setText("R$ " + String.format("%.02f", gasto.getValorTotal()));
        holder.odometro.setText(String.format("%.0f", gasto.getOdometro()) + "km");
        holder.motivo.setText(gasto.getMotivo());
        holder.local.setText(gasto.getLocal());

        boolean isExpanded = listaGasto.get(position).isExpanded();
        holder.subItem.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.btn_delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Gasto g = gasto;
                g.setTipo("gasto");

                Call<Void> requestDelete = autocasherAPI.deleteGasto(g);

                requestDelete.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if(!response.isSuccessful()){
                            Log.e(TAG, "Erro: " + response.code());
                            return;
                        } else{
                            Toast.makeText(context, "GASTO REMOVIDO COM SUCESSO",Toast.LENGTH_SHORT).show();
                            listaGasto.remove(position);
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

        holder.btn_edit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i = new Intent(v.getContext(), EditarGastoActivity.class);
                i.putExtra("Gasto", gasto);
                v.getContext().startActivity(i);
            }
        });


    }

    @Override
    public int getItemCount() {
        return listaGasto.size();
    }

    public class GastoViewHolder extends RecyclerView.ViewHolder{
        TextView observacao;
        TextView local;
        TextView motivo;
        TextView dateTime;
        TextView valorTotal;
        TextView tipo;
        TextView id;
        TextView odometro;
        LinearLayout subItem;
        LinearLayout gastoCard;
        ImageButton btn_edit;
        ImageButton btn_delete;


        public GastoViewHolder(@NonNull View itemView) {
            super(itemView);

            observacao = itemView.findViewById(R.id.observacao_textView);
            valorTotal = itemView.findViewById(R.id.valor_textView);
            tipo = itemView.findViewById(R.id.tipoRegistro_textView);
            dateTime = itemView.findViewById(R.id.data_textView);
            subItem = itemView.findViewById(R.id.sub_item);
            gastoCard = itemView.findViewById(R.id.gastoCard);
            local = itemView.findViewById(R.id.local_textView);
            motivo = itemView.findViewById(R.id.motivo_textView);
            odometro = itemView.findViewById(R.id.odometro_textView);
            btn_edit = itemView.findViewById(R.id.edit_imageView);
            btn_delete = itemView.findViewById(R.id.delete_imageView);

            gastoCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Gasto gasto = listaGasto.get(getAdapterPosition());
                    gasto.setExpanded(!gasto.isExpanded());
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
