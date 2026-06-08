package com.quiniela.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import com.quiniela.pojo.Usuario;

/**
 * Interceptor que protege TODAS las rutas /admin/** de forma centralizada.
 */
public class AdminInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AdminInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null)
                ? (Usuario) session.getAttribute("usuarioLogueado")
                : null;

        if (usuario == null || !"ADMIN".equals(usuario.getRol())) {
            log.warn("Acceso denegado a {} — usuario: {}",
                    request.getRequestURI(),
                    usuario != null ? usuario.getCorreoElectronico() : "no autenticado");
            response.sendRedirect(request.getContextPath() + "/?errorPermiso=true");
            return false;
        }
        return true;
    }
}
