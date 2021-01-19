package com.melobarros.autocasher.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Toast;


import com.google.android.material.textfield.TextInputEditText;
import com.melobarros.autocasher.R;
import com.melobarros.autocasher.fragment.DatePickerFragment;
import com.melobarros.autocasher.model.Gasto;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

@RequiresApi(api = Build.VERSION_CODES.O)
public class EditarGastoActivity extends AppCompatActivity implements DatePickerFragment.TheListener{

    public TextInputEditText tipoGasto, valorGasto, dataGasto, localGasto, infoAdicionalGasto, odometroGasto;
    public Button btnSalvar, btnDescartar;
    public ImageButton btnDataPicker;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy");


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
        setTexts(gasto);

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
                // TO DO
            }
        });
    }

    public void setTexts(Gasto gasto){
        tipoGasto.setText(gasto.getObservacao());
        valorGasto.setText(String.valueOf(gasto.getValorTotal()));
        dataGasto.setText(gasto.getLocalDateTime().format(formatter));
        localGasto.setText(gasto.getLocal());
        infoAdicionalGasto.setText(gasto.getMotivo());
        odometroGasto.setText(String.valueOf(gasto.getOdometro()));
    }

    public void initComponentes(){
        tipoGasto = findViewById(R.id.tipoGasto_input);
        valorGasto = findViewById(R.id.valorGasto_input);
        dataGasto = findViewById(R.id.dataGasto_input);
        localGasto = findViewById(R.id.localGasto_input);
        infoAdicionalGasto = findViewById(R.id.infoAdicionalGasto_input);
        odometroGasto = findViewById(R.id.odometro_input);
        btnDescartar = findViewById(R.id.descartarGasto_button);
        btnSalvar = findViewById(R.id.salvarGasto_button);
        btnDataPicker = findViewById(R.id.dataPicker_imageButton);
    }

    @Override
    public void returnDate(String date) {
        dataGasto.setText(date);
    }
}
