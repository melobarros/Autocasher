package com.melobarros.autocasher.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Gasto extends Registro implements Serializable {
    private float odometro;
    private String motivo;
    private float valorTotal;
    private boolean expanded;



    public Gasto(float odometro, String motivo, float valorTotal) {
        this.odometro = odometro;
        this.motivo = motivo;
        this.valorTotal = valorTotal;
        this.expanded = false;
    }

    public Gasto(){

    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public float getOdometro() {
        return odometro;
    }

    public void setOdometro(float odometro) {
        this.odometro = odometro;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public float getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(float valorTotal) {
        this.valorTotal = valorTotal;
    }
}
