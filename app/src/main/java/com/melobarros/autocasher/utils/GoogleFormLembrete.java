package com.melobarros.autocasher.utils;

public class GoogleFormLembrete {
    private String id;
    private String descricao;
    private String valorPrevisto;
    private String dataLembrete;
    private String local;
    private String observacao;
    private String acao;
    private String url;

    public GoogleFormLembrete(String id, String descricao, String valorPrevisto, String dataLembrete, String local, String observacao, String acao, String url) {
        this.id = id;
        this.descricao = descricao;
        this.valorPrevisto = valorPrevisto;
        this.dataLembrete = dataLembrete;
        this.local = local;
        this.observacao = observacao;
        this.acao = acao;
        this.url = url;
    }

    public GoogleFormLembrete(){

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getValorPrevisto() {
        return valorPrevisto;
    }

    public void setValorPrevisto(String valorPrevisto) {
        this.valorPrevisto = valorPrevisto;
    }

    public String getDataLembrete() {
        return dataLembrete;
    }

    public void setDataLembrete(String dataLembrete) {
        this.dataLembrete = dataLembrete;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }
}
