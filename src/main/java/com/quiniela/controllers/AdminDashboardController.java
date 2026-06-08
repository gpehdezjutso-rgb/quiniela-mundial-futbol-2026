package com.quiniela.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.quiniela.pojo.Partido;
import com.quiniela.pojo.Prediccion;
import com.quiniela.pojo.Usuario;
import com.quiniela.service.PartidoService;
import com.quiniela.service.PrediccionService;
import com.quiniela.service.UsuarioService;

/**
 * Panel principal de administración y cálculo de puntos globales.
 * Las rutas /admin/** quedan protegidas por AdminInterceptor.
 */
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private PartidoService partidoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PrediccionService prediccionService;

    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
        model.addAttribute("nombreUsuario", usuarioActual.getNombre());

        List<Partido> todosPartidos = partidoService.listarPartidos();
        if (todosPartidos == null) todosPartidos = new ArrayList<>();

        List<Usuario> todosUsuarios = usuarioService.listarTodosLosUsuarios();
        if (todosUsuarios == null) todosUsuarios = new ArrayList<>();

        long partidosConResultado = todosPartidos.stream()
                .filter(p -> p.getGolesLocal() != null).count();
        long partidosPendientes = todosPartidos.size() - partidosConResultado;

        List<Prediccion> todasPredicciones = prediccionService.obtenerTodas();
        if (todasPredicciones == null) todasPredicciones = new ArrayList<>();

        model.addAttribute("totalUsuarios", todosUsuarios.size());
        model.addAttribute("totalPartidos", todosPartidos.size());
        model.addAttribute("partidosConResultado", partidosConResultado);
        model.addAttribute("partidosPendientes", partidosPendientes);
        model.addAttribute("totalPredicciones", todasPredicciones.size());

        return "admin-dashboard";
    }

    @PostMapping("/puntos/calcular")
    public String calcularPuntosGlobales() {
        List<Prediccion> todas = prediccionService.obtenerTodas();
        usuarioService.procesarPuntosGlobales(todas);
        
        partidoService.generarTablaGeneralMundial();
        
        return "redirect:/admin/dashboard?calculoExitoso=true";
    }
}
