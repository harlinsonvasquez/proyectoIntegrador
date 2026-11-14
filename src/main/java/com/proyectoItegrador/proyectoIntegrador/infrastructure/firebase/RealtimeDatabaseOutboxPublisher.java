package com.proyectoItegrador.proyectoIntegrador.infrastructure.firebase;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.OutboxEventoEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.OutboxEventoJpaRepository;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Publica idempotentemente cada evento de outbox como notificación en Realtime Database. */
@Component
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
public class RealtimeDatabaseOutboxPublisher {
    private static final Logger LOGGER = Logger.getLogger(RealtimeDatabaseOutboxPublisher.class.getName());
    private final OutboxEventoJpaRepository eventos;
    private final ObjectMapper mapper;
    private final DatabaseReference estudiantesRef;

    public RealtimeDatabaseOutboxPublisher(OutboxEventoJpaRepository eventos, ObjectMapper mapper, FirebaseApp app) {
        this.eventos = eventos;
        this.mapper = mapper;
        this.estudiantesRef = FirebaseDatabase.getInstance(app).getReference("estudiantes");
    }

    @Scheduled(fixedDelayString = "${firebase.outbox-delay-ms:5000}")
    public void publicarPendientes() {
        eventos.findTop100ByProcesadoEnIsNullOrderByCreadoEnAsc().forEach(this::publicar);
    }

    private void publicar(OutboxEventoEntity evento) {
        try {
            Map<String, Object> data = mapper.convertValue(evento.getPayload(), new TypeReference<>() {});
            Object keyValue = data.get("codigoEstudiantil");
            if (!(keyValue instanceof String studentKey) || studentKey.isBlank()) {
                throw new IllegalStateException("El evento no incluye el código del estudiante destinatario.");
            }
            data.put("creadaEn", Date.from(evento.getCreadoEn().toInstant()).toInstant().toString());
            data.put("creadaEnEpoch", evento.getCreadoEn().toInstant().toEpochMilli());
            data.put("leida", false);
            DatabaseReference notification = estudiantesRef.child(studentKey)
                    .child("notificaciones").child(evento.getId().toString());
            notification.setValueAsync(data).get();
            evento.marcarProcesado();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            evento.registrarError("Publicación a Realtime Database interrumpida.");
        } catch (ExecutionException | RuntimeException ex) {
            LOGGER.log(Level.WARNING, "No se pudo publicar evento outbox " + evento.getId(), ex);
            evento.registrarError(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
        }
        eventos.save(evento);
    }
}
