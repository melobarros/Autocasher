package com.melobarros.autocasher.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.melobarros.autocasher.R;

public class CalcularMelhorCombustivelActivity extends AppCompatActivity {

    public EditText precoGasolina, precoAlcool;
    public Button calcularMelhorPreco;
    public TextView melhorCombustivel;

    String combustivelTemp;
    Float gasolinaTemp, alcoolTemp, resultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calcular_melhor_combustivel);

        setupDisplaySize();
        initComponentes();

        calcularMelhorPreco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                combustivelTemp = getMelhorCombustivel();
                melhorCombustivel.setText(combustivelTemp);
            }
        });

    }

    public String getMelhorCombustivel(){
        String combustivel = "";

        if (precoAlcool.getText().toString() != "" && precoGasolina.getText().toString() != "") {
            gasolinaTemp = Float.valueOf(precoGasolina.getText().toString());
            alcoolTemp = Float.valueOf(precoAlcool.getText().toString());
            resultado = alcoolTemp / gasolinaTemp;

            if(resultado < 0.67){
                combustivel = "Álcool";

            }else{
                combustivel = "Gasolina";
            }
        }else{
            Toast.makeText(CalcularMelhorCombustivelActivity.this, "INSIRA O VALOR DOS DOIS COMBUSTIVEIS",Toast.LENGTH_SHORT).show();
        }

        return combustivel;
    }

    public void setupDisplaySize(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width * 0.65), (int)(height * 0.65));
    }

    public void initComponentes(){
        precoGasolina = findViewById(R.id.precoGasolina_input);
        precoAlcool = findViewById(R.id.precoAlcool_input);
        calcularMelhorPreco = findViewById(R.id.calcularMelhorCombustivel_button);
        melhorCombustivel = findViewById(R.id.melhorCombustivel_textView);
    }
}
