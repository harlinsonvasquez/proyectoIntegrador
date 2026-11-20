package com.proyectoItegrador.proyectoIntegrador.application.service;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoMatricula;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.RolNombre;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.EstudianteJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.NotaParcialJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.NotaParcialDtos.NotaParcialEstudianteResponse;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsultaNotasParcialesService {
    private final EstudianteJpaRepository estudiantes;
    private final NotaParcialJpaRepository notas;

    @Transactional(readOnly = true)
    public List<NotaParcialEstudianteResponse> misNotas(AuthenticatedUser actor) {
        if (actor.role() != RolNombre.ESTUDIANTE) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN,
                    "Solo el estudiante autenticado puede consultar sus notas parciales.");
        }
        var estudiante = estudiantes.findByUsuario_Id(actor.userId()).orElseThrow(() ->
                new ReglaNegocioException(HttpStatus.NOT_FOUND, "Perfil de estudiante no encontrado."));
        return notas.findAllByMatricula_Estudiante_IdOrderByIdDesc(estudiante.getId()).stream()
                .map(nota -> {
                    var evaluacion = nota.getEvaluacion();
                    var oferta = evaluacion.getOferta();
                    var asignatura = oferta.getPlanAsignatura().getAsignatura();
                    BigDecimal aporte = nota.getNota().multiply(evaluacion.getPorcentaje())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    return new NotaParcialEstudianteResponse(nota.getId(), nota.getMatricula().getId(),
                            asignatura.getCodigo(), asignatura.getNombre(), oferta.getPeriodo().getNombre(),
                            oferta.getGrupo(), evaluacion.getNombre(), evaluacion.getPorcentaje(),
                            nota.getNota(), aporte, nota.getMatricula().getNotaFinal(),
                            nota.getObservacion(), nota.getActualizadaEn());
                }).toList();
    }
}
