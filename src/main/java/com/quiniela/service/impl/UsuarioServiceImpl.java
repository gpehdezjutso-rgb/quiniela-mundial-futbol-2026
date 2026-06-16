package com.quiniela.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.quiniela.dao.PrediccionDao;
import com.quiniela.dao.UsuarioDao;
import com.quiniela.pojo.Partido;
import com.quiniela.pojo.Prediccion;
import com.quiniela.pojo.Usuario;
import com.quiniela.service.UsuarioService;
import com.quiniela.pojo.RankingEjecutivo;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private PrediccionDao prediccionDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public boolean registrarUsuario(Usuario usuario) {
        if (usuario.getPassword() == null || usuario.getPassword().trim().length() < 6) {
            return false;
        }
        if (usuarioDao.buscarPorCorreo(usuario.getCorreoElectronico()) != null) {
            return false;
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setPuntosTotales(0);
        usuarioDao.guardar(usuario);
        log.info("Nuevo usuario registrado: {}", usuario.getCorreoElectronico());
        return true;
    }

    @Override
    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioDao.buscarPorId(id);
    }

    @Override
    public List<Usuario> obtenerTablaPosiciones() {
        List<Usuario> todos = usuarioDao.obtenerTodos();
        if (todos == null) return new ArrayList<>();
        return todos.stream()
                .sorted((u1, u2) -> u2.getPuntosTotales().compareTo(u1.getPuntosTotales()))
                .collect(Collectors.toList());
    }

    @Override
    public void actualizarPerfil(Usuario usuario) {
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            if (!usuario.getPassword().startsWith("$2a$")) {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }
        } else {
            // Campo vacío: conservamos el hash actual para no perderlo
            Usuario uActual = usuarioDao.buscarPorId(usuario.getId());
            if (uActual != null) {
                usuario.setPassword(uActual.getPassword());
            }
        }
        usuarioDao.actualizar(usuario);
    }

    @Override
    public Usuario autenticar(String correo, String password) {
        if (correo == null || password == null) return null;
        Usuario usuario = usuarioDao.buscarPorCorreo(correo.trim());
        if (usuario != null && passwordEncoder.matches(password.trim(), usuario.getPassword())) {
            return usuario;
        }
        return null;
    }

    @Override
      public void procesarPuntosGlobales(List<Prediccion> todasLasPredicciones) {
        List<Usuario> todosLosUsuarios = usuarioDao.obtenerTodos();
        
        for (Prediccion p : todasLasPredicciones) {
            Partido partido = p.getPartido();
            if (partido.getGolesLocal() == null || partido.getGolesVisitante() == null) continue;

            int realL = partido.getGolesLocal();
            int realV = partido.getGolesVisitante();
            int predL = p.getGolesLocalPrediccion();
            int predV = p.getGolesVisitantePrediccion();
            
            int puntosObtenidos = 0;
            if (realL == predL && realV == predV) {
                puntosObtenidos = 3;
            } else if ((realL > realV && predL > predV)
                    || (realL < realV && predL < predV)
                    || (realL == realV && predL == predV)) {
                puntosObtenidos = 1;
            }

            p.setPuntosGanados(puntosObtenidos);
           
            Usuario jugador = p.getUsuario();
            if (jugador != null) {
                jugador.setPuntosTotales(puntosObtenidos);                
                prediccionDao.actualizar(p);
            }
        }
        
     // Resetear puntos
        for (Usuario u : todosLosUsuarios) {
            u.setPuntosTotales(0);
            int totalPuntos = prediccionDao.sumarPuntosPorUsuario(u.getId());
            u.setPuntosTotales(totalPuntos);           	
            usuarioDao.actualizar(u);            	
        }        
                
        log.info("Puntos globales calculados para {} predicciones", todasLasPredicciones.size());
    }

    @Override
    public List<Usuario> listarTodosLosUsuarios() {
        return usuarioDao.obtenerTodos();
    }

    @Override
    public void eliminarUsuario(Long id) {
        usuarioDao.eliminar(id);
    }

      @Override
    public List<RankingEjecutivo> obtenerRankingEjecutivo() {
        List<Usuario> usuarios = usuarioDao.obtenerTodos();
        List<Prediccion> todasPredicciones = prediccionDao.obtenerTodas();

        if (usuarios == null) usuarios = new ArrayList<>();
        if (todasPredicciones == null) todasPredicciones = new ArrayList<>();

        List<RankingEjecutivo> resultado = new ArrayList<>();

        for (Usuario u : usuarios) {
            List<Prediccion> predsUsuario = todasPredicciones.stream()
                    .filter(p -> p.getUsuario() != null && p.getUsuario().getId().equals(u.getId()))
                    .collect(Collectors.toList());

            int exactos = 0;
            int parciales = 0;
            int jugados = 0;
            int pronosticados = predsUsuario.size();

            for (Prediccion p : predsUsuario) {
                Partido partido = p.getPartido();
                if (partido != null && partido.getGolesLocal() != null) {
                    jugados++;
                    if (p.getPuntosGanados() == 3) exactos++;
                    else if (p.getPuntosGanados() == 1) parciales++;
                }
            }

            resultado.add(new RankingEjecutivo(
                    u.getNombre(), u.getPuntosTotales(), exactos, parciales, pronosticados, jugados));
        }

        resultado.sort((a, b) -> Integer.compare(b.getPuntosTotales(), a.getPuntosTotales()));

        return resultado;
    }
}
