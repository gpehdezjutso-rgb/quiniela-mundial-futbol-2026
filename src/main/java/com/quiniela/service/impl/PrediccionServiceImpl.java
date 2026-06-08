package com.quiniela.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quiniela.dao.PartidoDao;
import com.quiniela.dao.PrediccionDao;
import com.quiniela.dao.UsuarioDao;
import com.quiniela.pojo.Partido;
import com.quiniela.pojo.Prediccion;
import com.quiniela.pojo.ResultadoApuesta;
import com.quiniela.pojo.Usuario;
import com.quiniela.service.PrediccionService;

@Service
public class PrediccionServiceImpl implements PrediccionService {

    private static final Logger log = LoggerFactory.getLogger(PrediccionServiceImpl.class);

    @Autowired
    private PrediccionDao prediccionDao;

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private PartidoDao partidoDao;

    @Override
    @Transactional
    public ResultadoApuesta guardarApuesta(Long usuarioId, Long partidoId,
                                           Integer golesLocal, Integer golesVisitante) {
        Usuario usuario = usuarioDao.buscarPorId(usuarioId);
        Partido partido = partidoDao.buscarPorId(partidoId);

        if (usuario == null || partido == null) {
            return ResultadoApuesta.DATOS_INVALIDOS;
        }

        if (partido.getGolesLocal() != null) {
            return ResultadoApuesta.PARTIDO_CON_RESULTADO;
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (partido.getFechaPartido() == null || !ahora.isBefore(partido.getFechaPartido())) {
            return ResultadoApuesta.PARTIDO_EXPIRADO;
        }

        Prediccion prediccion = prediccionDao.buscarPorUsuarioYPartido(usuarioId, partidoId);
        if (prediccion == null) {
            prediccion = new Prediccion();
            prediccion.setUsuario(usuario);
            prediccion.setPartido(partido);
        }
        prediccion.setGolesLocalPrediccion(golesLocal);
        prediccion.setGolesVisitantePrediccion(golesVisitante);
        prediccionDao.guardar(prediccion);

        log.debug("Apuesta guardada: usuario={} partido={} {}-{}", usuarioId, partidoId, golesLocal, golesVisitante);
        return ResultadoApuesta.EXITO;
    }

    @Override
    public List<Prediccion> listarApuestasUsuario(Long usuarioId) {
        return prediccionDao.buscarPorUsuario(usuarioId);
    }

    @Override
    public List<Prediccion> obtenerTodas() {
        return prediccionDao.obtenerTodas();
    }

    @Override
    public int[] obtenerEstadisticasUsuario(Long usuarioId) {
        List<Prediccion> apuestas = prediccionDao.buscarPorUsuario(usuarioId);
        if (apuestas == null) return new int[]{0, 0};

        int exactos = 0;
        int parciales = 0;
        for (Prediccion p : apuestas) {
            if (p.getPartido().getGolesLocal() != null && p.getPuntosGanados() != null) {
                if (p.getPuntosGanados() == 3) exactos++;
                else if (p.getPuntosGanados() == 1) parciales++;
            }
        }
        return new int[]{exactos, parciales};
    }
}
