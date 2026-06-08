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
import com.quiniela.pojo.Pais;
import com.quiniela.service.CatalogosService;

/**
 * Gestión de catálogos (Fases y Países) desde el panel de administración.
 * MEJORA: eliminación de fase/pais migrada a POST para evitar borrados accidentales.
 */
@Controller
@RequestMapping("/admin/catalogos")
public class AdminCatalogosController {

    @Autowired
    private CatalogosService catalogosService;

    @Value("#{'${mundial.fases.estatus}'.split(',')}")
    private List<String> listaFasesEstatus;

    @GetMapping
    public String catalogos(@RequestParam(defaultValue = "paises") String tab, Model model) {
        model.addAttribute("tab", tab);
        model.addAttribute("fase", new Fase());
        model.addAttribute("paisAlta", new Pais());
        model.addAttribute("paises", catalogosService.listarPaises());
        model.addAttribute("fases", catalogosService.listarFases());
        model.addAttribute("faseEstatus", listaFasesEstatus);
        return "admin-catalogos";
    }

    @PostMapping("/fases")
    public String guardarFase(@ModelAttribute Fase fase) {
        catalogosService.registrarFase(fase);
        return "redirect:/admin/catalogos?tab=fases";
    }

    /**
     * MEJORA: eliminación de fase por POST en lugar de GET.
     * Actualizar el template admin-catalogos.html para usar un form POST.
     */
    @PostMapping("/fases/eliminar")
    public String eliminarFase(@RequestParam Long id) {
        catalogosService.eliminarFase(id);
        return "redirect:/admin/catalogos?tab=fases";
    }

    @PostMapping("/fases/estado")
    public String actualizarEstadoFase(@RequestParam Long id, @RequestParam String estado) {
        Fase fase = catalogosService.obtenerFasePorId(id);
        fase.setEstado(estado);
        catalogosService.registrarFase(fase);
        return "redirect:/admin/catalogos?tab=fases";
    }

    @PostMapping("/paises")
    public String guardarPais(@ModelAttribute Pais pais) {
        catalogosService.registrarPais(pais);
        return "redirect:/admin/catalogos?tab=paises";
    }

    @PostMapping("/paises/estado")
    public String actualizarEstadoPais(@RequestParam Long id, @RequestParam String estado) {
        Pais pais = catalogosService.obtenerPaisPorId(id);
        pais.setEstado(estado);
        catalogosService.registrarPais(pais);
        return "redirect:/admin/catalogos?tab=paises";
    }
}
