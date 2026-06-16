package com.quiniela.pojo;

public class RankingEjecutivo {
    private String nombre;
    private int puntosTotales;
    private int exactos;
    private int parciales;
    private int pronosticados;
    private int jugados;
    private double efectividad;

    public RankingEjecutivo(String nombre, int puntosTotales, int exactos, int parciales, int pronosticados, int jugados) {
        this.nombre = nombre;
        this.puntosTotales = puntosTotales;
        this.exactos = exactos;
        this.parciales = parciales;
        this.pronosticados = pronosticados;
        this.jugados = jugados;
        this.efectividad = (jugados > 0)
                ? ((exactos + parciales) * 100.0 / jugados)
                : 0.0;
    }

    public String getNombre() { return nombre; }
    public int getPuntosTotales() { return puntosTotales; }
    public int getExactos() { return exactos; }
    public int getParciales() { return parciales; }
    public int getPronosticados() { return pronosticados; }
    public int getJugados() { return jugados; }
    public double getEfectividad() { return efectividad; }
}