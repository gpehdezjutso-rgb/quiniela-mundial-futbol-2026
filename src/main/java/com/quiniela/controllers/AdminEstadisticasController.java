package com.quiniela.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.quiniela.pojo.Fase;
import com.quiniela.pojo.PrediccionResumenDTO;
import com.quiniela.pojo.RankingEjecutivo;
import com.quiniela.pojo.Usuario;
import com.quiniela.service.CatalogosService;
import com.quiniela.service.UsuarioService;

@Controller
@RequestMapping("/admin/estadisticas")
public class AdminEstadisticasController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CatalogosService catalogosService;
    
    @GetMapping
    public String estadisticas(HttpSession session, Model model,
            @RequestParam(required = false) Long faseId,
            @RequestParam(required = false, defaultValue = "ranking") String tab) {

        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioActual == null) return "redirect:/?errorSesion=true";

        if (faseId == null) {
            Fase faseActiva = catalogosService.obtenerFaseActiva();
            if (faseActiva != null) faseId = faseActiva.getId();
        }

        List<Fase> todasFases = catalogosService.listarFases();
        if (todasFases == null) todasFases = new ArrayList<>();

        List<RankingEjecutivo> ranking = faseId != null
                ? usuarioService.obtenerRankingEjecutivoPorFase(faseId)
                : new ArrayList<>();

        List<PrediccionResumenDTO> resumen = faseId != null
                ? usuarioService.obtenerResumenPrediccionesPorFase(faseId)
                : new ArrayList<>();

        List<Usuario> listaJugadores = usuarioService.obtenerTablaPosiciones();
        if (listaJugadores == null) listaJugadores = new ArrayList<>();

        model.addAttribute("todasFases", todasFases);
        model.addAttribute("faseSeleccionada", faseId);
        model.addAttribute("rankingEjecutivo", ranking);
        model.addAttribute("resumen", resumen);
        model.addAttribute("tabActiva", tab);
        model.addAttribute("nombreUsuario", usuarioActual.getNombre());
        model.addAttribute("resultadosJugadores", listaJugadores);
        
        return "admin-estadisticas";
    }
}
