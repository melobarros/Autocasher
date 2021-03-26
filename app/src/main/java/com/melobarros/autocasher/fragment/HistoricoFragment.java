package com.melobarros.autocasher.fragment;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.melobarros.autocasher.R;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoricoFragment extends Fragment implements AdapterView.OnItemSelectedListener  {
    private static final String TAG = "HistoricoFragment";
    Toolbar toolbar;

    private Spinner ordenarPor_spinner, periodo_spinner;
    private static final String[] ordernarPor_paths = {"Ordernar por", "Mais novos", "Mais antigos", "Maior valor", "Menor valor"};
    private static final String[] periodo_paths = {"Período", "15 dias", "30 dias", "90 dias", "1 ano", "2 anos", "5 anos"};
    String selectedSpinner;

    public HistoricoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: started.");
        View view = inflater.inflate(R.layout.fragment_historico, container, false);

        toolbar = view.findViewById(R.id.Estatisticas_toolbar);
        initToolbar();

        return view;
    }

    public void initToolbar(){
        toolbar.setTitle("");
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
