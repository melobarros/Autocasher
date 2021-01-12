package com.melobarros.autocasher.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.melobarros.autocasher.R;
import com.melobarros.autocasher.model.Gasto;

import java.util.List;

public class AdapterGasto extends RecyclerView.Adapter<AdapterGasto.GastoViewHolder> {

    private List<Gasto> listaGasto;
    private Context context;

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

    @Override
    public void onBindViewHolder(@NonNull GastoViewHolder holder, int position) {
        Gasto gasto = listaGasto.get(position);

        holder.tipo.setText(gasto.getTipo());
        holder.dateTime.setText(gasto.getDateTime());
        holder.observacao.setText(gasto.getObservacao());
        holder.valorTotal.setText(String.valueOf(gasto.getValorTotal()));
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


        public GastoViewHolder(@NonNull View itemView) {
            super(itemView);

            observacao = itemView.findViewById(R.id.observacao_textView);
            valorTotal = itemView.findViewById(R.id.valor_textView);
            tipo = itemView.findViewById(R.id.tipoRegistro_textView);
            dateTime = itemView.findViewById(R.id.data_textView);
        }
    }
}
