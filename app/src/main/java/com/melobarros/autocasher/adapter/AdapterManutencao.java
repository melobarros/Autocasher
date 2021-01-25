package com.melobarros.autocasher.adapter;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.melobarros.autocasher.R;
import com.melobarros.autocasher.model.Gasto;
import com.melobarros.autocasher.model.Manutencao;
import com.melobarros.autocasher.services.autocasherAPI;

import java.time.format.DateTimeFormatter;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AdapterManutencao extends RecyclerView.Adapter<AdapterManutencao.ManutencaoViewHolder> {
    private static final String TAG = "AdapterManutencao";

    private List<Manutencao> manutencaoList;
    private Context context;
    Retrofit retrofit;
    autocasherAPI autocasherAPI;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy");

    public AdapterManutencao(List<Manutencao> l, Context c){
        this.manutencaoList = l;
        this.context = c;
    }

    @NonNull
    @Override
    public ManutencaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_manutencao, parent, false);
        initService();

        return new ManutencaoViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull ManutencaoViewHolder holder, int position) {
        final Manutencao manutencao = manutencaoList.get(position);

        holder.tipo.setText(manutencao.getTipo());
        holder.dateTime.setText(manutencao.getLocalDateTime().format(formatter));
        holder.observacao.setText(manutencao.getObservacao());
        holder.valorTotal.setText("R$ " + String.format("%.02f", manutencao.getValor()));
        holder.pecas.setText(manutencao.getPecas());
        holder.motivo.setText(manutencao.getDescricao());
        holder.local.setText(manutencao.getLocal());
        holder.subItem.setVisibility(manutencao.isExpanded() ? View.VISIBLE : View.GONE);

        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return manutencaoList.size();
    }

    public class ManutencaoViewHolder extends RecyclerView.ViewHolder{
        TextView observacao;
        TextView local;
        TextView motivo;
        TextView dateTime;
        TextView valorTotal;
        TextView tipo;
        TextView id;
        TextView pecas;
        LinearLayout subItem;
        LinearLayout manutencaoCard;
        ImageButton btn_edit;
        ImageButton btn_delete;


        public ManutencaoViewHolder(@NonNull View view){
            super(view);

            observacao = itemView.findViewById(R.id.manutencao_observacao_textView);
            valorTotal = itemView.findViewById(R.id.manutencao_valor_textView);
            tipo = itemView.findViewById(R.id.manutencao_tipoRegistro_textView);
            dateTime = itemView.findViewById(R.id.manutencao_data_textView);
            subItem = itemView.findViewById(R.id.manutencao_sub_item);
            manutencaoCard = itemView.findViewById(R.id.manutencaoCard);
            local = itemView.findViewById(R.id.manutencao_local_textView);
            motivo = itemView.findViewById(R.id.manutencao_motivo_textView);
            pecas = itemView.findViewById(R.id.manutencao_pecas_textView);
            btn_edit = itemView.findViewById(R.id.manutencao_edit_imageView);
            btn_delete = itemView.findViewById(R.id.manutencao_delete_imageView);

            manutencaoCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Manutencao manutencao = manutencaoList.get(getAdapterPosition());
                    manutencao.setExpanded(!manutencao.isExpanded());
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
