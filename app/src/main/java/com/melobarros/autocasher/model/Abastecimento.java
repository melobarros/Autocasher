package com.melobarros.autocasher.model;

public class Abastecimento extends Registro{
    private float odometro;
    private float precoLitro;
    private float valorTotal;
    private float litros;
    private boolean completandoTanque;
    private boolean abastecimentoAnteriorEmFalta;


    public float getOdometro() {
        return odometro;
    }

    public void setOdometro(float odometro) {
        this.odometro = odometro;
    }

    public float getPrecoLitro() {
        return precoLitro;
    }

    public void setPrecoLitro(float precoLitro) {
        this.precoLitro = precoLitro;
    }

    public float getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(float valorTotal) {
        this.valorTotal = valorTotal;
    }

    public float getLitros() {
        return litros;
    }

    public void setLitros(float litros) {
        this.litros = litros;
    }

    public boolean isCompletandoTanque() {
        return completandoTanque;
    }

    public void setCompletandoTanque(boolean completandoTanque) {
        this.completandoTanque = completandoTanque;
    }

    public boolean isAbastecimentoAnteriorEmFalta() {
        return abastecimentoAnteriorEmFalta;
    }

    public void setAbastecimentoAnteriorEmFalta(boolean abastecimentoAnteriorEmFalta) {
        this.abastecimentoAnteriorEmFalta = abastecimentoAnteriorEmFalta;
    }
}
