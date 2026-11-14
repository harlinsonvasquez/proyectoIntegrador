package com.proyectoItegrador.proyectoIntegrador.infrastructure.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.UsuarioEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.UsuarioJpaRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class FirebaseBearerFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FirebaseBearerFilter.class);

    private final FirebaseAuth firebaseAuth;
    private final UsuarioJpaRepository usuarios;

    public FirebaseBearerFilter(ObjectProvider<FirebaseAuth> firebaseAuth, UsuarioJpaRepository usuarios) {
        this.firebaseAuth = firebaseAuth.getIfAvailable();
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && !header.startsWith("Bearer ")) {
            log.warn("Authorization header rejected for {} {}: expected Bearer token",
                    request.getMethod(), request.getRequestURI());
        }
        if (firebaseAuth != null && header != null && header.startsWith("Bearer ")) {
            try {
                FirebaseToken token = firebaseAuth.verifyIdToken(header.substring(7), true);
                String email = token.getEmail();
                boolean emailVerified = Boolean.TRUE.equals(token.getClaims().get("email_verified"));
                if (email == null) {
                    log.warn("Firebase token accepted cryptographically but has no email claim for {} {}",
                            request.getMethod(), request.getRequestURI());
                } else if (!emailVerified) {
                    log.warn("Firebase account email is not verified for {} {}",
                            request.getMethod(), request.getRequestURI());
                } else {
                    UsuarioEntity usuario = usuarios.findByCorreoIgnoreCase(email).orElse(null);
                    if (usuario == null) {
                        log.warn("No local user matches verified Firebase account for {} {}",
                                request.getMethod(), request.getRequestURI());
                    } else if (!usuario.isActivo()) {
                        log.warn("Local user is inactive for {} {}", request.getMethod(), request.getRequestURI());
                    } else {
                        AuthenticatedUser principal = new AuthenticatedUser(
                                usuario.getId(), usuario.getCorreo(), usuario.getRol().getNombre());
                        var auth = new UsernamePasswordAuthenticationToken(principal, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name())));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (FirebaseAuthException e) {
                log.warn("Firebase rejected bearer token for {} {}: {}",
                        request.getMethod(), request.getRequestURI(), e.getMessage());
                SecurityContextHolder.clearContext();
            } catch (RuntimeException e) {
                log.error("Authentication lookup failed for {} {}: {}",
                        request.getMethod(), request.getRequestURI(), e.getMessage(), e);
                SecurityContextHolder.clearContext();
            }
        } else if (header != null && header.startsWith("Bearer ") && firebaseAuth == null) {
            log.error("Bearer token received but FirebaseAuth is not configured");
        }
        filterChain.doFilter(request, response);
    }
}
