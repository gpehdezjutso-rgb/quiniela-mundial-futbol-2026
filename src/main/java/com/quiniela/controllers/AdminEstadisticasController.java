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

import javax.servlet.http.HttpServletResponse;
import com.quiniela.util.ExcelExporter;
import com.quiniela.util.PdfExporter;
import com.lowagie.text.DocumentException;
import java.io.IOException;

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
    
    @GetMapping("/exportar-ranking-excel")
    public void exportarRankingExcel(HttpSession session, HttpServletResponse response,
            @RequestParam(required = false) Long faseId) throws IOException {

        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioActual == null) { response.sendError(403); return; }

        if (faseId == null) {
            Fase faseActiva = catalogosService.obtenerFaseActiva();
            if (faseActiva != null) faseId = faseActiva.getId();
        }

        List<RankingEjecutivo> ranking = faseId != null
                ? usuarioService.obtenerRankingEjecutivoPorFase(faseId)
                : new ArrayList<>();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=ranking_fase.xlsx");

        ExcelExporter exporter = new ExcelExporter("Ranking");
        exporter.writeHeader(new String[]{"#", "Jugador", "Pts Fase", "Exactos",
                "Parciales", "Registrados", "Faltantes", "Jugados", "% Efectividad"});

        List<String[]> data = new ArrayList<>();
        int pos = 1;
        for (RankingEjecutivo r : ranking) {
            data.add(new String[]{
                    String.valueOf(pos++),
                    r.getNombre(),
                    String.valueOf(r.getPuntosTotales()),
                    String.valueOf(r.getExactos()),
                    String.valueOf(r.getParciales()),
                    String.valueOf(r.getPronosticados()),
                    String.valueOf(r.getFaltantes()),
                    String.valueOf(r.getJugados()),
                    String.format("%.1f%%", r.getEfectividad())
            });
        }

        exporter.writeData(data);
        exporter.export(response);
    }
    
    @GetMapping("/exportar-predicciones-pdf")
    public void exportarPrediccionesPdf(HttpSession session, HttpServletResponse response,
            @RequestParam(required = false) Long faseId) throws IOException, DocumentException {

        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioActual == null) { response.sendError(403); return; }

        if (faseId == null) {
            Fase faseActiva = catalogosService.obtenerFaseActiva();
            if (faseActiva != null) faseId = faseActiva.getId();
        }

        List<PrediccionResumenDTO> resumen = faseId != null
                ? usuarioService.obtenerResumenPrediccionesPorFase(faseId)
                : new ArrayList<>();

        String[] headers = {"Partido", "Fecha", "Resultado", "Total Apuestas",
                "Más Votado", "Menos Votado", "Exactos", "Ganador/Empate", "Fallidos"};

        List<String[]> filas = new ArrayList<>();
        for (PrediccionResumenDTO r : resumen) {
            String resultado = r.getPartido().getGolesLocal() != null
                    ? r.getPartido().getGolesLocal() + " - " + r.getPartido().getGolesVisitante()
                    : "Por jugar";

            String fecha = r.getPartido().getFechaPartido() != null
                    ? r.getPartido().getFechaPartido().toString().replace("T", " ").substring(0, 16)
                    : "-";

            filas.add(new String[]{
                    r.getPartido().getEquipoLocal() + " vs " + r.getPartido().getEquipoVisitante(),
                    fecha,
                    resultado,
                    String.valueOf(r.getTotalApuestas()),
                    r.getMarcadorMasApostado() + " (" + r.getVotosMarcadorTop() + ")",
                    r.getMarcadorMenosApostado() + " (" + r.getVotosMarcadorBottom() + ")",
                    String.join(", ", r.getAcertadores().isEmpty() ? List.of("-") : r.getAcertadores()),
                    String.join(", ", r.getParcialesAcertadores().isEmpty() ? List.of("-") : r.getParcialesAcertadores()),
                    String.join(", ", r.getFallidos().isEmpty() ? List.of("-") : r.getFallidos())
            });
        }

        PdfExporter exporter = new PdfExporter("Predicciones por Partido");
        exporter.export(response, filas, headers, "Distribución de apuestas por fase");
    }
}
