package com.quiniela.controllers;

import java.time.ZoneId;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.quiniela.pojo.EstadisticaEquipo;
import com.quiniela.pojo.Fase;
import com.quiniela.pojo.PartidoDTO;
import com.quiniela.pojo.Partido;
import com.quiniela.pojo.Prediccion;
import com.quiniela.pojo.ResultadoApuesta;
import com.quiniela.pojo.Usuario;
import com.quiniela.service.CatalogosService;
import com.quiniela.service.PartidoService;
import com.quiniela.service.PrediccionService;
import com.quiniela.service.UsuarioService;
import com.quiniela.util.ExcelExporter;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import com.quiniela.util.ExcelExporter;

/**
 * Dashboard del usuario: listado de partidos, apuestas y tabla de posiciones.
 */
@Controller
public class DashboardController {

    @Autowired
    private PartidoService partidoService;

    @Autowired
    private PrediccionService prediccionService;
    
    @Autowired
    private CatalogosService catalogosService;

    @Autowired
    private UsuarioService usuarioService;

    /*
    @SuppressWarnings("null")
	@GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        
        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioActual == null) {
            return "redirect:/?errorSesion=true";
        }
        
     // Refrescar desde BD para evitar valores obsoletos de la sesión
        usuarioActual = usuarioService.obtenerUsuarioPorId(usuarioActual.getId());
        session.setAttribute("usuarioLogueado", usuarioActual);
        
        List<Fase> fasesActivas = catalogosService.obtenerFasesActivas();
        
        List<Long> fases = new ArrayList<>();
        
        for ( int i = 0; i < fasesActivas.size(); i++ ) {        
        	fases.add(fasesActivas.get(i).getId());
        }
        
        List<Partido> partidos = partidoService.obtenerPartidosPorFasesActivas(fases);
                
        //List<Partido> partidos = partidoService.listarPartidos();
        if (partidos == null) partidos = new ArrayList<>();
        
        List<Prediccion> apuestas = prediccionService.listarApuestasUsuario(usuarioActual.getId());
        
        if (apuestas == null) apuestas = new ArrayList<>();
        
        List<PartidoDTO> partidosConApuesta = new ArrayList<>();
        for (Partido p : partidos) {
            Prediccion apuestaEncontrada = apuestas.stream()
                    .filter(a -> a.getPartido().getId().equals(p.getId()))
                    .findFirst()
                    .orElse(null);
            partidosConApuesta.add(new PartidoDTO(p, apuestaEncontrada));
        }

        List<EstadisticaEquipo> listaEstadisticas = partidoService.obtenerTablaGeneralMundial();
        if (listaEstadisticas == null) listaEstadisticas = new ArrayList<>();

        List<Usuario> listaJugadores = usuarioService.obtenerTablaPosiciones();
        if (listaJugadores == null) listaJugadores = new ArrayList<>();

        int[] estadisticas = prediccionService.obtenerEstadisticasUsuario(usuarioActual.getId());

        model.addAttribute("partidosDTO", partidosConApuesta);
        model.addAttribute("usuarioActual", usuarioActual);
        model.addAttribute("apuestas", apuestas);
        model.addAttribute("aciertosExactos", estadisticas[0]);
        model.addAttribute("aciertosParciales", estadisticas[1]);
        model.addAttribute("titulo", "Quiniela Mundial 2026");
        model.addAttribute("resultadosJugadores", listaJugadores);
        model.addAttribute("tablaEquipos", listaEstadisticas);
        
        return "dashboard";
    }*/
    
    @SuppressWarnings("null")
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(value = "faseId", required = false) Long faseId,
                             HttpSession session, Model model) {

        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioActual == null) {
            return "redirect:/?errorSesion=true";
        }

        // Refrescar desde BD para evitar valores obsoletos de la sesión
        usuarioActual = usuarioService.obtenerUsuarioPorId(usuarioActual.getId());
        session.setAttribute("usuarioLogueado", usuarioActual);

        // ── Fases activas, ordenadas por el campo 'orden' ──
        List<Fase> fasesActivas = catalogosService.obtenerFasesActivas();
        if (fasesActivas == null) fasesActivas = new ArrayList<>();
        fasesActivas.sort(java.util.Comparator.comparing(Fase::getOrdenFase));

        // ── Determinar la fase seleccionada ──
        Fase faseSeleccionada = null;
        if (faseId != null) {
            final Long faseIdBuscada = faseId;
            faseSeleccionada = fasesActivas.stream()
                    .filter(f -> f.getId().equals(faseIdBuscada))
                    .findFirst()
                    .orElse(null);
        }
        if (faseSeleccionada == null) {
            // Default: la última fase registrada según 'orden'
            faseSeleccionada = fasesActivas.stream()
                    .max(java.util.Comparator.comparing(Fase::getOrdenFase))
                    .orElse(null);
        }

        // ── Partidos SOLO de la fase seleccionada ──
        List<Long> fases = new ArrayList<>();
        if (faseSeleccionada != null) {
            fases.add(faseSeleccionada.getId());
        }
        List<Partido> partidos = partidoService.obtenerPartidosPorFasesActivas(fases);
        if (partidos == null) partidos = new ArrayList<>();

        List<Prediccion> apuestas = prediccionService.listarApuestasUsuario(usuarioActual.getId());
        if (apuestas == null) apuestas = new ArrayList<>();

        List<PartidoDTO> partidosConApuesta = new ArrayList<>();
        for (Partido p : partidos) {
            Prediccion apuestaEncontrada = apuestas.stream()
                    .filter(a -> a.getPartido().getId().equals(p.getId()))
                    .findFirst()
                    .orElse(null);
            partidosConApuesta.add(new PartidoDTO(p, apuestaEncontrada));
        }

        List<EstadisticaEquipo> listaEstadisticas = partidoService.obtenerTablaGeneralMundial();
        if (listaEstadisticas == null) listaEstadisticas = new ArrayList<>();

        List<Usuario> listaJugadores = usuarioService.obtenerTablaPosiciones();
        if (listaJugadores == null) listaJugadores = new ArrayList<>();

        int[] estadisticas = prediccionService.obtenerEstadisticasUsuario(usuarioActual.getId());

        // ── NUEVO: posición del usuario actual dentro del ranking (1-based; 0 = no encontrado) ──
        int posicionActual = 0;
        for (int i = 0; i < listaJugadores.size(); i++) {
            if (listaJugadores.get(i).getId().equals(usuarioActual.getId())) {
                posicionActual = i + 1;
                break;
            }
        }

        model.addAttribute("partidosDTO", partidosConApuesta);
        model.addAttribute("usuarioActual", usuarioActual);
        model.addAttribute("apuestas", apuestas);
        model.addAttribute("aciertosExactos", estadisticas[0]);
        model.addAttribute("aciertosParciales", estadisticas[1]);
        model.addAttribute("titulo", "Quiniela Mundial 2026");
        model.addAttribute("resultadosJugadores", listaJugadores);
        model.addAttribute("tablaEquipos", listaEstadisticas);
        model.addAttribute("posicionActual", posicionActual);

        // ── NUEVO: para el combo de fases ──
        model.addAttribute("fasesActivas", fasesActivas);
        model.addAttribute("faseSeleccionada", faseSeleccionada);

        return "dashboard";
    }
    
    @ModelAttribute("ahora")
    public LocalDateTime ahoraAttribute() {
        return LocalDateTime.now(ZoneId.of("America/Mexico_City"));
    }

    /*
    @PostMapping("/dashboard/apuesta")
    public String guardarApuesta(@RequestParam("partidoId") Long partidoId,
                                 @RequestParam("golesLocalPrediccion") Integer golesLocalPrediccion,
                                 @RequestParam("golesVisitantePrediccion") Integer golesVisitantePrediccion,
                                 HttpSession session) {
        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioActual == null) {
            return "redirect:/?errorSesion=true";
        }
        
        Partido partido = partidoService.obtenerPartidoPorId(partidoId);
        
        LocalDateTime limiteApuesta = partido.getFechaPartido().minusHours(1);
        if (LocalDateTime.now(ZoneId.of("America/Mexico_City")).isAfter(limiteApuesta)) {
            return "redirect:/dashboard?errorApuesta=expirado";
        }

        ResultadoApuesta resultado = prediccionService.guardarApuesta(
                usuarioActual.getId(), partidoId, golesLocalPrediccion, golesVisitantePrediccion);
        
        

        switch (resultado) {
            case EXITO:               return "redirect:/dashboard?apuestaOk=true";
            case PARTIDO_CON_RESULTADO: return "redirect:/dashboard?errorApuesta=resultado";
            case PARTIDO_EXPIRADO:    return "redirect:/dashboard?errorApuesta=expirado";
            default:                  return "redirect:/dashboard?errorApuesta=invalido";
        }
    }
    */
    
    @PostMapping("/dashboard/apuesta")
    public String guardarApuesta(@RequestParam("partidoId") Long partidoId,
                                 @RequestParam("golesLocalPrediccion") Integer golesLocalPrediccion,
                                 @RequestParam("golesVisitantePrediccion") Integer golesVisitantePrediccion,
                                 @RequestParam(value = "faseId", required = false) Long faseId,
                                 HttpSession session) {
        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioActual == null) {
            return "redirect:/?errorSesion=true";
        }

        Partido partido = partidoService.obtenerPartidoPorId(partidoId);

        LocalDateTime limiteApuesta = partido.getFechaPartido().minusHours(1);
        String sufijoFase = (faseId != null) ? "&faseId=" + faseId : "";

        if (LocalDateTime.now(ZoneId.of("America/Mexico_City")).isAfter(limiteApuesta)) {
            return "redirect:/dashboard?errorApuesta=expirado" + sufijoFase;
        }

        ResultadoApuesta resultado = prediccionService.guardarApuesta(
                usuarioActual.getId(), partidoId, golesLocalPrediccion, golesVisitantePrediccion);

        switch (resultado) {
            case EXITO:               return "redirect:/dashboard?apuestaOk=true" + sufijoFase;
            case PARTIDO_CON_RESULTADO: return "redirect:/dashboard?errorApuesta=resultado" + sufijoFase;
            case PARTIDO_EXPIRADO:    return "redirect:/dashboard?errorApuesta=expirado" + sufijoFase;
            default:                  return "redirect:/dashboard?errorApuesta=invalido" + sufijoFase;
        }
    }
    
    @GetMapping("/dashboard/resultadosjugadores/exportar")
    public void exportarExcel(HttpServletResponse response)
            throws IOException { 	
    	
    	LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Mexico_City"));
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    	String formatted = now.format(formatter);
    	String nombreArchivo = "Ranking jugadores " + formatted; 

        response.setContentType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        response.setHeader(
            "Content-Disposition",
            "attachment; filename=" + nombreArchivo + ".xlsx");

        ExcelExporter exporter = new ExcelExporter(nombreArchivo);

        String[] headers = {
            "#",
            "Jugador",
            "Puntos"
        };

        exporter.writeHeader(headers);
        
        List<String[]> data = usuarioService.obtenerTablaPosiciones()
            .stream()
            .map(p -> new String[] {
                    String.valueOf(p.getId()),
                    p.getNombre().toString(),                    
                    p.getPuntosTotales().toString()
            })
            .toList();

        exporter.writeData(data);

        exporter.export(response);
    }
    
    @GetMapping("/dashboard/exportar-predicciones")
    public void exportarPredicciones(HttpSession session, HttpServletResponse response) throws java.io.IOException {

        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioActual == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // ── Reconstruir la misma data que usa el dashboard ──
        List<Fase> fasesActivas = catalogosService.obtenerFasesActivas();
        List<Long> fases = new ArrayList<>();
        for (Fase f : fasesActivas) {
            fases.add(f.getId());
        }

        List<Partido> partidos = partidoService.obtenerPartidosPorFasesActivas(fases);
        if (partidos == null) partidos = new ArrayList<>();

        List<Prediccion> apuestas = prediccionService.listarApuestasUsuario(usuarioActual.getId());
        if (apuestas == null) apuestas = new ArrayList<>();

        // ── Configurar respuesta HTTP ──
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=mis_predicciones.xlsx");

        // ── Generar Excel ──
        ExcelExporter exporter = new ExcelExporter("Mis Predicciones");

        String[] headers = {"Fase", "Grupo", "Encuentro", "Resultado Real", "Mi Predicción", "Puntos"};
        exporter.writeHeader(headers);

        List<String[]> data = new ArrayList<>();
        for (Partido p : partidos) {
            Prediccion pred = apuestas.stream()
                    .filter(a -> a.getPartido().getId().equals(p.getId()))
                    .findFirst()
                    .orElse(null);

            String fase = (p.getFase() != null) ? p.getFase().getNombre() : "-";
            String grupo = (p.getGrupo() != null) ? p.getGrupo() : "-";
            String encuentro = p.getEquipoLocal() + " vs " + p.getEquipoVisitante();

            String resultadoReal = (p.getGolesLocal() != null)
                    ? p.getGolesLocal() + " - " + p.getGolesVisitante()
                    : "Por jugar";

            String miPrediccion = (pred != null)
                    ? pred.getGolesLocalPrediccion() + " - " + pred.getGolesVisitantePrediccion()
                    : "Sin apuesta";

            String puntos = (pred != null && pred.getPuntosGanados() != null)
                    ? String.valueOf(pred.getPuntosGanados())
                    : "-";

            data.add(new String[]{fase, grupo, encuentro, resultadoReal, miPrediccion, puntos});
        }

        exporter.writeData(data);
        exporter.export(response);
    }
  
}
