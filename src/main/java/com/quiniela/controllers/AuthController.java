package com.quiniela.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.quiniela.pojo.Usuario;
import com.quiniela.service.UsuarioService;

/**
 * Maneja login, logout y registro de usuarios.
 */
@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public String mostrarLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam("correo") String correo,
                                @RequestParam("password") String password,
                                HttpSession session,
                                Model model) {
        Usuario usuario = usuarioService.autenticar(correo, password);

        if (usuario != null) {
            session.setAttribute("usuarioLogueado", usuario);
            return "ADMIN".equals(usuario.getRol())
                    ? "redirect:/admin/dashboard"
                    : "redirect:/dashboard";
        }

        model.addAttribute("error", "Correo electrónico o contraseña incorrectos.");
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute("usuario") Usuario usuario, Model model) {
        usuario.setRol("USER");

        if (usuario.getPassword() == null || usuario.getPassword().trim().length() < 6) {
            model.addAttribute("error", "La contraseña debe tener una longitud mínima de 6 caracteres.");
            return "registro";
        }

        boolean registroExitoso = usuarioService.registrarUsuario(usuario);

        if (!registroExitoso) {
            model.addAttribute("error", "El correo electrónico ya se encuentra registrado.");
            return "registro";
        }

        return "redirect:/?registroExitoso=true";
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/?logoutExitoso=true";
    }
}
