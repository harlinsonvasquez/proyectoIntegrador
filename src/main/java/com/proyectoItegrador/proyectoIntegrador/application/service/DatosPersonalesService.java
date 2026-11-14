package com.proyectoItegrador.proyectoIntegrador.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoItegrador.proyectoIntegrador.application.port.out.DatosPersonalesPort;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.OperacionAuditoria;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.RolNombre;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.ActualizarDatosPersonalesRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.DatosPersonalesResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DatosPersonalesService {
    private final DatosPersonalesPort persistence;
    private final ObjectMapper mapper;

    @Transactional
    public DatosPersonalesResponse actualizar(AuthenticatedUser actor, ActualizarDatosPersonalesRequest request) {
        if (actor.role() != RolNombre.ESTUDIANTE) throw new ReglaNegocioException(HttpStatus.FORBIDDEN,
                "Solo el estudiante puede modificar sus datos personales.");
        var user = persistence.usuario(actor.userId())
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        Map<String, Object> before = Map.of("nombre", user.getNombre(), "apellido", user.getApellido());
        user.actualizarDatosPersonales(request.nombre().trim(), request.apellido().trim());
        persistence.guardar(user);
        persistence.guardarAuditoria(new com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.AuditoriaCambioEntity(
                user, "usuario", user.getId().toString(), OperacionAuditoria.UPDATE, mapper.valueToTree(before),
                mapper.valueToTree(Map.of("nombre", user.getNombre(), "apellido", user.getApellido()))));
        return new DatosPersonalesResponse(user.getId(), user.getNombre(), user.getApellido(), user.getCorreo());
    }
}
