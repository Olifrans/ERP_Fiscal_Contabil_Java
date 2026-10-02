package com.unipiagte.uniescola.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "uniescola.config")
public class UniescolaProperties {
    private String nomeSistema;
    private String versao;
    private int maxAlunosTurma;
    private String ambiente;

    public String getNomeSistema() { return nomeSistema; }
    public void setNomeSistema(String nomeSistema) { this.nomeSistema = nomeSistema; }
    public String getVersao() { return versao; }
    public void setVersao(String versao) { this.versao = versao; }
    public int getMaxAlunosTurma() { return maxAlunosTurma; }
    public void setMaxAlunosTurma(int maxAlunosTurma) { this.maxAlunosTurma = maxAlunosTurma; }
    public String getAmbiente() { return ambiente; }
    public void setAmbiente(String ambiente) { this.ambiente = ambiente; }
}