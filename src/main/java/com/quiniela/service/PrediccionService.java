package com.quiniela.service;

import java.util.List;

import com.quiniela.pojo.Prediccion;
import com.quiniela.pojo.ResultadoApuesta;

public interface PrediccionService {
    ResultadoApuesta guardarApuesta(Long usuarioId, Long partidoId,
                                    Integer golesLocal, Integer golesVisitante);
    List<Prediccion> listarApuestasUsuario(Long usuarioId);
    int[] obtenerEstadisticasUsuario(Long usuarioId);
    /** Devuelve todas las predicciones (para cálculo de puntos y métricas admin). */
    List<Prediccion> obtenerTodas();
}
