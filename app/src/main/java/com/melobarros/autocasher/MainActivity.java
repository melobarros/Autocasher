package com.melobarros.autocasher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.melobarros.autocasher.fragment.AbastecimentoFragment;
import com.melobarros.autocasher.fragment.GastoFragment;
import com.melobarros.autocasher.fragment.HistoricoFragment;
import com.melobarros.autocasher.fragment.LembreteFragment;
import com.melobarros.autocasher.fragment.ManutencaoFragment;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar bottom navigation view
        configuraBottomNavigationView();

        // Start Historico Fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.viewPager, new HistoricoFragment()).commit();



        /*
        - TO DO
            - Melhorias:
                - Geral
                    - Filtrar por data e ordenar por valor e data
                    - Implementar tela de Estatisticas
                    - Arrumar casos de uso em que usuario preenche campo com valor invalido (vazio, texto, numero)
                    - Opcional: circulo em volta dos icones
                - Gasto
                - Lembrete
                    - Definir qual sera o metodo de lembrete (notificacao android, email, outro?)
                - Manutencao
                    - ?
            - Dashboards com MPAndroidChart

         */
    }

    private void configuraBottomNavigationView(){
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavigation);

        bottomNavigationViewEx.enableAnimation(true);

        habilitarNavegacao(bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);
    }

    private void habilitarNavegacao(BottomNavigationViewEx viewEx){
        viewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                switch(menuItem.getItemId()){
                    case R.id.ic_historico :
                        fragmentTransaction.replace(R.id.viewPager, new HistoricoFragment()).commit();
                        return true;
                    case R.id.ic_abastecimento :
                        fragmentTransaction.replace(R.id.viewPager, new AbastecimentoFragment()).commit();
                        return true;
                    case R.id.ic_lembrete :
                        fragmentTransaction.replace(R.id.viewPager, new LembreteFragment()).commit();
                        return true;
                    case R.id.ic_gasto :
                        fragmentTransaction.replace(R.id.viewPager, new GastoFragment()).commit();
                        return true;
                    case R.id.ic_manutencao :
                        fragmentTransaction.replace(R.id.viewPager, new ManutencaoFragment()).commit();
                        return true;
                }

                return false;
            }
        });
    }
}
