package com.melobarros.autocasher.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
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

import com.melobarros.autocasher.R;
import com.melobarros.autocasher.activity.EditarGastoActivity;
import com.melobarros.autocasher.model.Gasto;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AdapterGasto extends RecyclerView.Adapter<AdapterGasto.GastoViewHolder> {

    private List<Gasto> listaGasto;
    private Context context;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy HH:mm:ss");

    public AdapterGasto(List<Gasto> l, Context c) {
        this.listaGasto = l;
        this.context = c;
    }

    @NonNull
    @Override
    public GastoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_gasto, parent, false);

        return new GastoViewHolder(itemLista);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull GastoViewHolder holder, int position) {
        final Gasto gasto = listaGasto.get(position);

        holder.tipo.setText(gasto.getTipo());
        holder.dateTime.setText(gasto.getLocalDateTime().format(formatter));
        holder.observacao.setText(gasto.getObservacao());
        holder.valorTotal.setText(String.valueOf(gasto.getValorTotal()));
        holder.odometro.setText(String.valueOf(gasto.getOdometro()));
        holder.motivo.setText(gasto.getMotivo());
        holder.local.setText(gasto.getLocal());

        boolean isExpanded = listaGasto.get(position).isExpanded();
        holder.subItem.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.btn_delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(context, "Delete Clicked!", Toast.LENGTH_LONG).show();
            }
        });

        holder.btn_edit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i = new Intent(v.getContext(), EditarGastoActivity.class);
                //Toast.makeText(context, "Observacao: " + gasto.getObservacao(), Toast.LENGTH_LONG).show();
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
}
