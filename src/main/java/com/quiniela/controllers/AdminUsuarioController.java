package com.quiniela.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.quiniela.pojo.Usuario;
import com.quiniela.service.UsuarioService;

/**
 * Gestión CRUD de usuarios desde el panel de administración.
 */
@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String gestionarUsuarios(
            @RequestParam(value = "editarId", required = false) Long editarId,
            Model model) {

        Usuario usuarioForm = new Usuario();
        if (editarId != null) {
            Usuario usuarioExistente = usuarioService.obtenerUsuarioPorId(editarId);
            if (usuarioExistente != null) {
                usuarioForm = usuarioExistente;
            }
        }

        model.addAttribute("usuario", usuarioForm);
        model.addAttribute("usuarios", usuarioService.listarTodosLosUsuarios());
        return "admin-usuarios";
    }

    @PostMapping
    public String guardarOActualizarUsuario(@ModelAttribute("usuario") Usuario usuario) {
        if (usuario.getId() != null) {
            Usuario uBD = usuarioService.obtenerUsuarioPorId(usuario.getId());
            if (uBD != null) {
                uBD.setNombre(usuario.getNombre());
                uBD.setCorreoElectronico(usuario.getCorreoElectronico());
                uBD.setRol(usuario.getRol());
                if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                    uBD.setPassword(usuario.getPassword());
                }
                usuarioService.actualizarPerfil(uBD);
            }
        }
        return "redirect:/admin/usuarios";
    }

    /** POST para eliminar — nunca GET. */
    @PostMapping("/eliminar")
    public String eliminarUsuario(@RequestParam("id") Long id) {
        usuarioService.eliminarUsuario(id);
        return "redirect:/admin/usuarios";
    }
}
