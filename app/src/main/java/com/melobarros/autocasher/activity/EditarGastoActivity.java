package com.melobarros.autocasher.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.material.textfield.TextInputEditText;
import com.melobarros.autocasher.R;
import com.melobarros.autocasher.model.Gasto;

public class EditarGastoActivity extends AppCompatActivity {

    public TextInputEditText tipoGasto, valorGasto, dataGasto, localGasto, infoAdicionalGasto, odometroGasto;
    public Button btnSalvar, btnDescartar;


    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_gasto);

        Toolbar toolbar = findViewById(R.id.EditarGasto_toolbar);
        toolbar.setTitle("Editar Gasto");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);

        initComponentes();

        Gasto gasto = (Gasto)getIntent().getSerializableExtra("Gasto");
        tipoGasto.setText(gasto.getObservacao());
        //Toast.makeText(getApplicationContext(), "Observacao: " + gasto.getObservacao(), Toast.LENGTH_LONG).show();
        valorGasto.setText(String.valueOf(gasto.getValorTotal()));
        //dataGasto.setText();
        localGasto.setText(gasto.getLocal());
        infoAdicionalGasto.setText(gasto.getMotivo());
        odometroGasto.setText(String.valueOf(gasto.getOdometro()));

        //to do - fields, connect api
        // 4 other screens
        // dashboards
        // maybe filtering
    }

    public void initComponentes(){
        tipoGasto = findViewById(R.id.tipoGasto_input);
        valorGasto = findViewById(R.id.valorGasto_input);
        dataGasto = findViewById(R.id.dataGasto_input);
        localGasto = findViewById(R.id.localGasto_input);
        infoAdicionalGasto = findViewById(R.id.infoAdicionalGasto_input);
        odometroGasto = findViewById(R.id.odometro_input);
    }
}
