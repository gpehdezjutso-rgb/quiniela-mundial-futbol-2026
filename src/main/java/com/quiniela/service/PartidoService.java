package com.quiniela.service;

import java.util.List;

import com.quiniela.pojo.EstadisticaEquipo;
import com.quiniela.pojo.Partido;

public interface PartidoService {
    void registrarPartido(Partido partido);
    List<Partido> listarPartidos();
    void actualizarResultado(Long id, Integer golesLocal, Integer golesVisitante);
    void actualizarPartido(Partido partido);
    Partido obtenerPartidoPorId(Long id);
    void eliminarPartido(Long id);
    List<EstadisticaEquipo> obtenerTablaGeneralMundial();
    List<Partido> obtenerPorFase(Long faseId);
    void eliminarResultado(Long id);
	List<EstadisticaEquipo> generarTablaGeneralMundial();
    void vaciarTabla();	
	List<Partido> obtenerPartidosPorFasesActivas(List<Long> faseId);
}
