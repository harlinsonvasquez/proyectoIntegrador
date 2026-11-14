package com.proyectoItegrador.proyectoIntegrador.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoItegrador.proyectoIntegrador.application.port.out.SolicitudPersistencePort;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoMatricula;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstudianteEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.MatriculaAsignaturaEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.RolNombre;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.CrearSolicitudRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class SolicitudApplicationServiceTest {
    @Test
    void rechazaCancelacionDeMatriculaNoActivaSinCrearSolicitud() {
        SolicitudPersistencePort persistence = mock(SolicitudPersistencePort.class);
        SolicitudApplicationService service = new SolicitudApplicationService(persistence, new ObjectMapper());
        AuthenticatedUser actor = new AuthenticatedUser(4L, "estudiante@ejemplo.edu", RolNombre.ESTUDIANTE);
        EstudianteEntity student = mock(EstudianteEntity.class);
        MatriculaAsignaturaEntity enrollment = mock(MatriculaAsignaturaEntity.class);
        when(student.getId()).thenReturn(44L);
        when(persistence.estudiantePorUsuario(4L)).thenReturn(Optional.of(student));
        when(persistence.matriculaDelEstudiante(9L, 44L)).thenReturn(Optional.of(enrollment));
        when(enrollment.getEstado()).thenReturn(EstadoMatricula.APROBADA);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> service.crear(actor, new CrearSolicitudRequest(9L, "Motivo de prueba")));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        verify(persistence, never()).guardar(any());
    }
}
