package com.quiniela.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quiniela.dao.PartidoDao;
import com.quiniela.dao.PosicionesMundialDao;
import com.quiniela.pojo.EstadisticaEquipo;
import com.quiniela.pojo.Partido;
import com.quiniela.service.PartidoService;

@Service
public class PartidoServiceImpl implements PartidoService {

    private static final Logger log = LoggerFactory.getLogger(PartidoServiceImpl.class);

    @Autowired
    private PartidoDao partidoDao;

    @Autowired
    private PosicionesMundialDao posicionesMundialDao;

    @Override
    public void registrarPartido(Partido partido) {
        partidoDao.guardar(partido);
    }

    @Override
    public List<Partido> listarPartidos() {
        return partidoDao.obtenerTodos();
    }

    @Override
    public List<Partido> obtenerPorFase(Long faseId) {
        return partidoDao.obtenerPorFase(faseId);
    }
    
    @Override
    public List<Partido> obtenerPartidosPorFasesActivas(List<Long>  faseId) {
    	   if (faseId == null || faseId.isEmpty()) {
    	        return new ArrayList<>();
    	    }
    	
        return partidoDao.obtenerPartidosPorFasesActivas(faseId);
    }

    @Override
    public void actualizarResultado(Long id, Integer golesLocal, Integer golesVisitante) {
        Partido partido = partidoDao.buscarPorId(id);
        if (partido != null) {
            partido.setGolesLocal(golesLocal);
            partido.setGolesVisitante(golesVisitante);
            partidoDao.actualizar(partido);
        }
    }

    /** MEJORA: persiste todos los campos de un partido editado (no solo el resultado). */
    @Override
    public void actualizarPartido(Partido partido) {
        partidoDao.actualizar(partido);
    }

    @Override
    public void eliminarResultado(Long id) {
        Partido partido = partidoDao.buscarPorId(id);
        if (partido != null) {
            partido.setGolesLocal(null);
            partido.setGolesVisitante(null);
            partidoDao.actualizar(partido);
        }
    }

    @Override
    public Partido obtenerPartidoPorId(Long id) {
        return partidoDao.buscarPorId(id);
    }

    @Override
    public void eliminarPartido(Long id) {
        partidoDao.eliminar(id);
    }

    /**
     * MEJORA: Lee primero de la tabla persistida en BD (calculada por el admin).
     * Solo recalcula si la tabla está vacía (primer arranque o reset manual).
     * Esto elimina el recálculo + escritura masiva en cada carga del dashboard.
     */
    @Override
    @Transactional
    public List<EstadisticaEquipo> generarTablaGeneralMundial() {
    	
    	posicionesMundialDao.vaciarTabla();
       
        // 2. Solo si la tabla está vacía, recalculamos desde los partidos
        log.info("Tabla de posiciones vacía — recalculando desde partidos");
        List<Partido> partidos = partidoDao.obtenerTodos();

        if (partidos == null || partidos.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, EstadisticaEquipo> mapaEstadisticas = new HashMap<>();
        for (Partido p : partidos) {
            if (p.getGolesLocal() != null && p.getGolesVisitante() != null) {
                String local = p.getEquipoLocal();
                String visitante = p.getEquipoVisitante();
                int gLocal = p.getGolesLocal();
                int gVisitante = p.getGolesVisitante();

                mapaEstadisticas.putIfAbsent(local, new EstadisticaEquipo(local));
                mapaEstadisticas.putIfAbsent(visitante, new EstadisticaEquipo(visitante));

                mapaEstadisticas.get(local).acumularPartido(gLocal, gVisitante);
                mapaEstadisticas.get(visitante).acumularPartido(gVisitante, gLocal);
            }
        }

        // Persistimos para que las próximas llamadas lean de BD
        for (EstadisticaEquipo est : mapaEstadisticas.values()) {
            posicionesMundialDao.guardarOActualizar(est);
        }

        log.info("Tabla recalculada y persistida ({} equipos)", mapaEstadisticas.size());
        return posicionesMundialDao.obtenerTablaOrdenada();
    }
    
    public void vaciarTabla() {
    	posicionesMundialDao.vaciarTabla();
    }
    
    /**
     * MEJORA: Lee primero de la tabla persistida en BD (calculada por el admin).
     * Solo recalcula si la tabla está vacía (primer arranque o reset manual).
     * Esto elimina el recálculo + escritura masiva en cada carga del dashboard.
     */
    @Override
    @Transactional
    public List<EstadisticaEquipo> obtenerTablaGeneralMundial() {
        log.debug("obtenerTablaGeneralMundial: consultando tabla persistida");

        // 1. Leer primero de la tabla en BD — path feliz en producción
        List<EstadisticaEquipo> tablaGuardada = posicionesMundialDao.obtenerTablaOrdenada();
        if (tablaGuardada != null && !tablaGuardada.isEmpty()) {
            log.debug("Tabla leída de BD ({} equipos)", tablaGuardada.size());
            return tablaGuardada;
        }

       
        return tablaGuardada;
    }
}
