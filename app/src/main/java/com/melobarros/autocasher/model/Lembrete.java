package com.melobarros.autocasher.model;

public class Lembrete extends Registro {
    private String descricao;
    private int repetirCada;
    private float valorPrevisto;

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getRepetirCada() {
        return repetirCada;
    }

    public void setRepetirCada(int repetirCada) {
        this.repetirCada = repetirCada;
    }

    public float getValorPrevisto() {
        return valorPrevisto;
    }

    public void setValorPrevisto(float valorPrevisto) {
        this.valorPrevisto = valorPrevisto;
    }
}

