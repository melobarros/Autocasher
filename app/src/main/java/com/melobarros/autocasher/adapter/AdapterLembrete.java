package com.melobarros.autocasher.adapter;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.melobarros.autocasher.R;
import com.melobarros.autocasher.model.Lembrete;
import com.melobarros.autocasher.model.Manutencao;
import com.melobarros.autocasher.services.autocasherAPI;

import java.time.format.DateTimeFormatter;
import java.util.List;

import retrofit2.Retrofit;

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
        

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull LembreteViewHolder holder, int position) {

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
}
