package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.AdministracionCrudService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.AdminDtos.AuditoriaResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.AdminDtos.CambiarRolRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.AdminDtos.UsuarioResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
@Tag(name = "Administración - Consultas", description = "Consultas administrativas de cuentas y trazabilidad.")
@SecurityRequirement(name = "firebaseBearer")
public class AdministracionConsultaController {
    private final AdministracionCrudService service;

    @GetMapping("/usuarios")
    @Operation(summary = "Listar usuarios", description = "Consulta las cuentas locales, sus roles y estado activo.")
    public List<UsuarioResponse> usuarios() { return service.usuarios(); }

    @PatchMapping("/usuarios/{id}/rol")
    @Operation(summary = "Cambiar rol de usuario",
            description = "Cambia el rol local y registra el antes/después en auditoría. Al asignar DOCENTE crea el perfil docente si no existe, conservando el perfil estudiantil y su historial. No permite cambiar el propio rol ni degradar al último administrador.")
    public UsuarioResponse cambiarRol(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable Long id,
            @Valid @RequestBody CambiarRolRequest request) {
        return service.cambiarRol(actor, id, request);
    }

    @GetMapping("/auditoria")
    @Operation(summary = "Consultar auditoría", description = "Lista cambios registrados; permite filtrar por entidad, registro, usuario y paginación.")
    public Page<AuditoriaResponse> auditoria(@RequestParam(required = false) String entidad,
            @RequestParam(required = false) String registroId, @RequestParam(required = false) Long usuarioId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        return service.auditoria(entidad, registroId, usuarioId, page, size);
    }
}
