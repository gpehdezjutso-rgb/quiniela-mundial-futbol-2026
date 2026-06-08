package com.quiniela.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor que configura el timeout de sesión a 30 minutos.
 * Sin esto, Tomcat usa su default (usualmente indefinido o muy largo).
 */
public class SessionTimeoutInterceptor implements HandlerInterceptor {

    private static final int SESSION_TIMEOUT_SECONDS = 1800; // 30 minutos

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
        }
        return true;
    }
}
