package com.quiniela.controllers;

import java.time.ZoneId;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

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

    @SuppressWarnings("null")
	@GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
	    	
        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioActual == null) {
            return "redirect:/?errorSesion=true";
        }
        
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
    }
    
    @ModelAttribute("ahora")
    public LocalDateTime ahoraAttribute() {
        return LocalDateTime.now(ZoneId.of("America/Mexico_City"));
    }

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
}
