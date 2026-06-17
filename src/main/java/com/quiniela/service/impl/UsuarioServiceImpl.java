package com.quiniela.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.quiniela.pojo.PrediccionResumenDTO;
import com.quiniela.pojo.RankingEjecutivo;
import com.quiniela.pojo.Usuario;
import com.quiniela.service.UsuarioService;

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
        
        System.out.println("procesarPuntosGlobales -> inicia");
        for (Prediccion p : todasLasPredicciones) {
            Partido partido = p.getPartido();
            if (partido.getGolesLocal() == null || partido.getGolesVisitante() == null) continue;

            int realL = partido.getGolesLocal();
            int realV = partido.getGolesVisitante();
            int predL = p.getGolesLocalPrediccion();
            int predV = p.getGolesVisitantePrediccion();
            
            System.out.println("realL -> " + realL);
            System.out.println("realV -> " + realV);
            System.out.println("predL -> " + predL);
            System.out.println("predV -> " + predV);

            int puntosObtenidos = 0;
            if (realL == predL && realV == predV) {
                puntosObtenidos = 3;
            } else if ((realL > realV && predL > predV)
                    || (realL < realV && predL < predV)
                    || (realL == realV && predL == predV)) {
                puntosObtenidos = 1;
            }

            p.setPuntosGanados(puntosObtenidos);
            System.out.println("puntosObtenidos -> " + puntosObtenidos);
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
        
        System.out.println("procesarPuntosGlobales -> termina");
        System.out.println("Puntos globales calculados para {} predicciones" + todasLasPredicciones.size());
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
    public List<RankingEjecutivo> obtenerRankingEjecutivoPorFase(Long faseId) {
        List<Usuario> usuarios = usuarioDao.obtenerTodos();
        List<Prediccion> todasPredicciones = prediccionDao.obtenerTodasConPartido();

        if (usuarios == null) usuarios = new ArrayList<>();
        if (todasPredicciones == null) todasPredicciones = new ArrayList<>();

        // Total de partidos en la fase
        long totalPartidosFase = todasPredicciones.stream()
                .map(p -> p.getPartido())
                .filter(p -> p.getFase() != null && p.getFase().getId().equals(faseId))
                .map(p -> p.getId())
                .distinct()
                .count();

        List<RankingEjecutivo> resultado = new ArrayList<>();

        for (Usuario u : usuarios) {
            // Predicciones del usuario en esta fase
            List<Prediccion> predsUsuarioFase = todasPredicciones.stream()
                    .filter(p -> p.getUsuario().getId().equals(u.getId())
                            && p.getPartido().getFase() != null
                            && p.getPartido().getFase().getId().equals(faseId))
                    .collect(Collectors.toList());

            int exactos = 0, parciales = 0, jugados = 0;

            for (Prediccion p : predsUsuarioFase) {
                if (p.getPartido().getGolesLocal() != null) {
                    jugados++;
                    if (p.getPuntosGanados() == 3) exactos++;
                    else if (p.getPuntosGanados() == 1) parciales++;
                }
            }

            // Puntos del usuario en esta fase
            int puntosFase = predsUsuarioFase.stream()
                    .mapToInt(p -> p.getPuntosGanados() != null ? p.getPuntosGanados() : 0)
                    .sum();

            String nombreFase = todasPredicciones.stream()
                    .filter(p -> p.getPartido().getFase() != null
                            && p.getPartido().getFase().getId().equals(faseId))
                    .map(p -> p.getPartido().getFase().getNombre())
                    .findFirst()
                    .orElse("Fase " + faseId);

            resultado.add(new RankingEjecutivo(
                    u.getNombre(), nombreFase, puntosFase,
                    exactos, parciales,
                    predsUsuarioFase.size(),
                    jugados,
                    (int) totalPartidosFase));
        }

        resultado.sort((a, b) -> Integer.compare(b.getPuntosTotales(), a.getPuntosTotales()));

        return resultado;
    }
    
    @Override
    public List<PrediccionResumenDTO> obtenerResumenPrediccionesPorFase(Long faseId) {
        List<Prediccion> todasPredicciones = prediccionDao.obtenerTodasConPartido();
        if (todasPredicciones == null) todasPredicciones = new ArrayList<>();

        // Agrupar predicciones por partido de la fase
        Map<Long, List<Prediccion>> porPartido = new LinkedHashMap<>();
        Map<Long, Partido> mapaPartidos = new LinkedHashMap<>();

        for (Prediccion p : todasPredicciones) {
            Partido partido = p.getPartido();
            if (partido.getFase() == null || !partido.getFase().getId().equals(faseId)) continue;

            porPartido.computeIfAbsent(partido.getId(), k -> new ArrayList<>()).add(p);
            mapaPartidos.put(partido.getId(), partido);
        }

        List<PrediccionResumenDTO> resultado = new ArrayList<>();

        for (Map.Entry<Long, List<Prediccion>> entry : porPartido.entrySet()) {
            Partido partido = mapaPartidos.get(entry.getKey());
            List<Prediccion> preds = entry.getValue();

            // Distribución de marcadores
            Map<String, Integer> distribucion = new LinkedHashMap<>();
            for (Prediccion p : preds) {
                String marcador = p.getGolesLocalPrediccion() + "-" + p.getGolesVisitantePrediccion();
                distribucion.merge(marcador, 1, Integer::sum);
            }

            // Marcador más apostado
            String marcadorTop = distribucion.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("-");
            int votosTop = distribucion.getOrDefault(marcadorTop, 0);

            // Acertadores exactos y parciales
            List<String> exactos = new ArrayList<>();
            List<String> parciales = new ArrayList<>();
            List<String> fallidos = new ArrayList<>();

            for (Prediccion p : preds) {
                if (p.getPuntosGanados() == null) continue;
                String nombre = p.getUsuario().getNombre();
                if (p.getPuntosGanados() == 3) exactos.add(nombre);
                else if (p.getPuntosGanados() == 1) parciales.add(nombre);
                else if (p.getPuntosGanados() == 0
                && p.getPartido().getGolesLocal() != null) fallidos.add(nombre);;
            }
            
            String marcadorBottom = distribucion.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("-");
            int votosBottom = distribucion.getOrDefault(marcadorBottom, 0);

            resultado.add(new PrediccionResumenDTO(
                    partido, preds.size(), marcadorTop, votosTop,
                    marcadorBottom, votosBottom,
                    exactos, parciales, fallidos, distribucion));
        }

        // Ordenar por fecha del partido
        resultado.sort((a, b) -> {
            if (a.getPartido().getFechaPartido() == null) return 1;
            if (b.getPartido().getFechaPartido() == null) return -1;
            return a.getPartido().getFechaPartido().compareTo(b.getPartido().getFechaPartido());
        });

        return resultado;
    }
}
