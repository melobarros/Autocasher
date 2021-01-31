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
import com.melobarros.autocasher.activity.EditarAbastecimentoActivity;
import com.melobarros.autocasher.model.Abastecimento;

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
public class AdapterAbastecimento extends RecyclerView.Adapter<AdapterAbastecimento.AbastecimentoViewHolder> {
    private static final String TAG = "AdapterAbastecimento";

    private List<Abastecimento> abastecimentoList;
    private Context context;
    Retrofit retrofit;
    com.melobarros.autocasher.services.autocasherAPI autocasherAPI;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy");

    public AdapterAbastecimento(List<Abastecimento> a, Context c){
        this.abastecimentoList = a;
        this.context = c;
    }

    @NonNull
    @Override
    public AbastecimentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_abastecimento, parent, false);
        initService();

        return new AbastecimentoViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull AbastecimentoViewHolder holder, int position) {
        final Abastecimento abastecimento = abastecimentoList.get(position);

        holder.litros.setText(String.format("%.0f",abastecimento.getLitros()) + "L");
        holder.valorLitro.setText("R$ " + String.format("%.2f", abastecimento.getPrecoLitro()));
        holder.data.setText(abastecimento.getLocalDateTime().format(formatter));
        holder.tanqueCheio.setText(String.valueOf(abastecimento.isCompletandoTanque()));
        holder.odometro.setText(String.format("%.0f", abastecimento.getOdometro()));
        holder.subItem.setVisibility(abastecimento.isExpanded() ? View.VISIBLE : View.GONE);
        holder.tipoRegistro.setText(abastecimento.getTipo());
        holder.valorTotal.setText("R$ " + String.format("%.2f", abastecimento.getLitros() * abastecimento.getPrecoLitro()));
        // to do

        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Abastecimento a = abastecimento;
                a.setTipo("abastecimento");

                Call<Void> requestDelete = autocasherAPI.deleteAbastecimento(a);
                requestDelete.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if(!response.isSuccessful()){
                            Log.e(TAG, "Erro: " + response.code());
                            return;
                        } else{
                            Toast.makeText(context, "ABASTECIMENTO REMOVIDO COM SUCESSO",Toast.LENGTH_SHORT).show();
                            abastecimentoList.remove(position);
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
                Intent i = new Intent(v.getContext(), EditarAbastecimentoActivity.class);
                i.putExtra("Abastecimento", abastecimento);
                v.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return abastecimentoList.size();
    }

    public class AbastecimentoViewHolder extends RecyclerView.ViewHolder {
        TextView litros;
        TextView valorLitro;
        TextView data;
        TextView tanqueCheio;
        TextView odometro;
        TextView valorTotal;
        TextView tipoRegistro;
        LinearLayout subItem;
        LinearLayout abastecimentoCard;
        ImageButton btn_edit;
        ImageButton btn_delete;

        public AbastecimentoViewHolder(@NonNull View itemView) {
            super(itemView);

            litros = itemView.findViewById(R.id.abastecimento_litros_textView);
            valorLitro = itemView.findViewById(R.id.abastecimento_valorLitro_textView);
            data = itemView.findViewById(R.id.abastecimento_data_textView);
            tanqueCheio = itemView.findViewById(R.id.abastecimento_InfoAdicional_textView);
            odometro = itemView.findViewById(R.id.abastecimento_odometro_textView);
            btn_edit = itemView.findViewById(R.id.abastecimento_edit_imageView);
            btn_delete = itemView.findViewById(R.id.abastecimento_delete_imageView);
            abastecimentoCard = itemView.findViewById(R.id.abastecimentoCard);
            subItem = itemView.findViewById(R.id.abastecimento_sub_item);
            valorTotal = itemView.findViewById(R.id.abastecimento_valor_textView);
            tipoRegistro = itemView.findViewById(R.id.abastecimento_tipoRegistro_textView);

            abastecimentoCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Abastecimento abastecimento = abastecimentoList.get(getAdapterPosition());
                    abastecimento.setExpanded(!abastecimento.isExpanded());
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
