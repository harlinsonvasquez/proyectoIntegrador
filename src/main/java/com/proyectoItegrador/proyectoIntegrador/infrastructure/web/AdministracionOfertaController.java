package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.AdministracionCrudService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.AdminDtos.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
@Tag(name = "Administración - Periodos y ofertas", description = "Configura periodos académicos y ofertas de asignaturas.")
@SecurityRequirement(name = "firebaseBearer")
public class AdministracionOfertaController {
    private final AdministracionCrudService service;

    @GetMapping("/periodos")
    @Operation(summary = "Listar periodos", description = "Devuelve los periodos académicos registrados.")
    public List<PeriodoResponse> periodos() { return service.periodos(); }
    @GetMapping("/periodos/{id}")
    @Operation(summary = "Consultar periodo", description = "Obtiene un periodo académico por su ID.")
    public PeriodoResponse periodo(@PathVariable Long id) { return service.periodo(id); }
    @PostMapping("/periodos")
    @Operation(summary = "Crear periodo", description = "Registra un periodo y sus fechas de inicio y fin.")
    @ResponseStatus(HttpStatus.CREATED)
    public PeriodoResponse crearPeriodo(@AuthenticationPrincipal AuthenticatedUser actor,
            @Valid @RequestBody PeriodoRequest r) { return service.crearPeriodo(actor, r); }
    @PutMapping("/periodos/{id}")
    @Operation(summary = "Actualizar periodo", description = "Modifica el código, nombre y fechas del periodo.")
    public PeriodoResponse actualizarPeriodo(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long id, @Valid @RequestBody PeriodoRequest r) {
        return service.actualizarPeriodo(actor, id, r);
    }
    @DeleteMapping("/periodos/{id}")
    @Operation(summary = "Eliminar periodo", description = "Elimina el periodo si no tiene ofertas dependientes.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPeriodo(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable Long id) {
        service.eliminarPeriodo(actor, id);
    }

    @GetMapping("/ofertas")
    @Operation(summary = "Listar ofertas", description = "Devuelve las ofertas académicas configuradas.")
    public List<OfertaResponse> ofertas() { return service.ofertas(); }
    @GetMapping("/ofertas/{id}")
    @Operation(summary = "Consultar oferta", description = "Obtiene una oferta por su ID.")
    public OfertaResponse oferta(@PathVariable Long id) { return service.oferta(id); }
    @PostMapping("/ofertas")
    @Operation(summary = "Crear oferta", description = "Programa una asignatura para un periodo, grupo, cupo y docente.")
    @ResponseStatus(HttpStatus.CREATED)
    public OfertaResponse crearOferta(@AuthenticationPrincipal AuthenticatedUser actor,
            @Valid @RequestBody OfertaRequest r) { return service.crearOferta(actor, r); }
    @PutMapping("/ofertas/{id}")
    @Operation(summary = "Actualizar oferta", description = "Modifica los datos de programación de la oferta.")
    public OfertaResponse actualizarOferta(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long id, @Valid @RequestBody OfertaRequest r) {
        return service.actualizarOferta(actor, id, r);
    }
    @DeleteMapping("/ofertas/{id}")
    @Operation(summary = "Eliminar oferta", description = "Elimina la oferta si no tiene matrículas dependientes.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarOferta(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable Long id) {
        service.eliminarOferta(actor, id);
    }
}
