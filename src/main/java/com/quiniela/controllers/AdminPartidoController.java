package com.quiniela.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.quiniela.pojo.Fase;
import com.quiniela.pojo.Partido;
import com.quiniela.service.CatalogosService;
import com.quiniela.service.PartidoService;

/**
 * Gestión CRUD de partidos desde el panel de administración.
 */
@Controller
@RequestMapping("/admin/partidos")
public class AdminPartidoController {

    @Autowired
    private PartidoService partidoService;

    @Autowired
    private CatalogosService catalogosService;

    @Value("#{'${mundial.grupos}'.split(',')}")
    private List<String> listaGrupos;

    @GetMapping
    public String gestionarPartidos(
            @RequestParam(value = "editarId", required = false) Long editarId,
            @RequestParam(required = false) Long faseId,
            Model model) {

        Partido partidoForm = new Partido();

        if (faseId == null) {
            Fase faseActiva = catalogosService.obtenerFaseActiva();
            if (faseActiva != null) {
                faseId = faseActiva.getId();
            }
        }

        if (editarId != null) {
            Partido partidoExistente = partidoService.obtenerPartidoPorId(editarId);
            if (partidoExistente != null) {
                partidoForm = partidoExistente;
                if (partidoExistente.getFechaPartido() != null) {
                    model.addAttribute("fechaEditar", partidoExistente.getFechaPartido().toString());
                }
            }
        }

        model.addAttribute("partido", partidoForm);
        model.addAttribute("partidos", partidoService.obtenerPorFase(faseId));
        model.addAttribute("estadios", catalogosService.obtenerEstadiosActivos());
        model.addAttribute("paises", catalogosService.obtenerActivosPais());
        model.addAttribute("fases", catalogosService.listarFases());
        model.addAttribute("fasesActivas", catalogosService.obtenerFasesActivas());
        model.addAttribute("faseSeleccionada", faseId);
        model.addAttribute("grupos", listaGrupos);

        return "admin-partidos";
    }

    /**
     * Guarda un partido nuevo o actualiza uno existente (todos sus campos).
     * MEJORA: ahora persiste correctamente equipo, fase y fecha al editar.
     */
    @PostMapping
    public String guardarOActualizarPartido(@ModelAttribute("partido") Partido partido,
                                            @RequestParam("fechaStr") String fechaStr) {
        if (fechaStr != null && !fechaStr.isEmpty()) {
            partido.setFechaPartido(java.time.LocalDateTime.parse(fechaStr));
        }

        if (partido.getId() != null) {
            // Edición: actualizamos todos los campos relevantes
            Partido p = partidoService.obtenerPartidoPorId(partido.getId());
            if (p != null) {
                p.setEquipoLocal(partido.getEquipoLocal());
                p.setEquipoVisitante(partido.getEquipoVisitante());
                p.setFase(partido.getFase());
                p.setFechaPartido(partido.getFechaPartido());
                p.setGrupo(partido.getGrupo());
                p.setEstadio(partido.getEstadio());
                // Solo actualizamos goles si vienen informados
                if (partido.getGolesLocal() != null) {
                    p.setGolesLocal(partido.getGolesLocal());
                    p.setGolesVisitante(partido.getGolesVisitante());
                }
                partidoService.actualizarPartido(p);
            }
        } else {
            partidoService.registrarPartido(partido);
        }

        return "redirect:/admin/partidos";
    }

    @PostMapping("/resultado")
    public String guardarResultado(@RequestParam("partidoId") Long partidoId,
                                   @RequestParam("golesLocal") Integer golesLocal,
                                   @RequestParam("golesVisitante") Integer golesVisitante) {
        partidoService.actualizarResultado(partidoId, golesLocal, golesVisitante);
        return "redirect:/admin/partidos";
    }

    @PostMapping("/resultado/eliminar")
    public String eliminarResultado(@RequestParam("partidoId") Long partidoId) {
        partidoService.eliminarResultado(partidoId);
        return "redirect:/admin/partidos";
    }

    /** POST para eliminar — nunca GET, para evitar borrados accidentales por crawlers. */
    @PostMapping("/eliminar")
    public String eliminarPartido(@RequestParam("id") Long id) {
        partidoService.eliminarPartido(id);
        return "redirect:/admin/partidos";
    }
}
