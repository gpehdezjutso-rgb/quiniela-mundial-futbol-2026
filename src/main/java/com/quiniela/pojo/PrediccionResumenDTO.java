package com.quiniela.pojo;

import java.util.List;
import java.util.Map;

public class PrediccionResumenDTO {

    private Partido partido;
    private int totalApuestas;
    private String marcadorMasApostado;
    private int votosMarcadorTop;
    private List<String> acertadores;         // nombres de quienes acertaron exacto
    private List<String> parcialesAcertadores; // nombres de quienes acertaron ganador/empate
    private Map<String, Integer> distribucion; // "2-1" -> 5 votos
    private List<String> fallidos; // quienes apostaron pero no acertaron nada
    private String marcadorMenosApostado;
    private int votosMarcadorBottom;

    public PrediccionResumenDTO(Partido partido, int totalApuestas, String marcadorMasApostado,
                                 int votosMarcadorTop, 
                                 String marcadorMenosApostado, int votosMarcadorBottom,
                                 List<String> acertadores,
                                 List<String> parcialesAcertadores,
                                 List<String> fallidos,
                                 Map<String, Integer> distribucion) {
        this.partido = partido;
        this.totalApuestas = totalApuestas;
        this.marcadorMasApostado = marcadorMasApostado;
        this.votosMarcadorTop = votosMarcadorTop;
        this.marcadorMenosApostado = marcadorMenosApostado;
        this.votosMarcadorBottom = votosMarcadorBottom;        
        this.acertadores = acertadores;
        this.parcialesAcertadores = parcialesAcertadores;
        this.fallidos = fallidos;
        this.distribucion = distribucion;
    }

    public Partido getPartido() { return partido; }
    public int getTotalApuestas() { return totalApuestas; }
    public String getMarcadorMasApostado() { return marcadorMasApostado; }
    public int getVotosMarcadorTop() { return votosMarcadorTop; }
    public List<String> getAcertadores() { return acertadores; }
    public List<String> getParcialesAcertadores() { return parcialesAcertadores; }
    public Map<String, Integer> getDistribucion() { return distribucion; }
    public List<String> getFallidos() { return fallidos; }
    public String getMarcadorMenosApostado() { return marcadorMenosApostado; }
    public int getVotosMarcadorBottom() { return votosMarcadorBottom; }
}