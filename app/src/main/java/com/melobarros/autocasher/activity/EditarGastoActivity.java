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
import com.melobarros.autocasher.model.Gasto;

import java.time.format.DateTimeFormatter;
import java.util.Calendar;

@RequiresApi(api = Build.VERSION_CODES.O)
public class EditarGastoActivity extends AppCompatActivity {

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

        tipoGasto.setText(gasto.getObservacao());
        //Toast.makeText(getApplicationContext(), "Observacao: " + gasto.getObservacao(), Toast.LENGTH_LONG).show();
        valorGasto.setText(String.valueOf(gasto.getValorTotal()));
        dataGasto.setText(gasto.getLocalDateTime().format(formatter));
        localGasto.setText(gasto.getLocal());
        infoAdicionalGasto.setText(gasto.getMotivo());
        odometroGasto.setText(String.valueOf(gasto.getOdometro()));

        btnDescartar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


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
        btnDescartar = findViewById(R.id.descartarGasto_button);
        btnSalvar = findViewById(R.id.salvarGasto_button);
        btnDataPicker = findViewById(R.id.dataPicker_imageButton);
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user

        }
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }


}
