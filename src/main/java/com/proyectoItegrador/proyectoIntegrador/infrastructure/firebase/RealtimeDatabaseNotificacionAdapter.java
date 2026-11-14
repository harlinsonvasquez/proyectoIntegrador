package com.proyectoItegrador.proyectoIntegrador.infrastructure.firebase;

import com.google.api.core.ApiFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.proyectoItegrador.proyectoIntegrador.application.port.out.NotificacionPort;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.EstudianteJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.NotificacionResponse;
import java.time.OffsetDateTime;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
public class RealtimeDatabaseNotificacionAdapter implements NotificacionPort {
    private final DatabaseReference estudiantesRef;
    private final EstudianteJpaRepository estudiantes;

    public RealtimeDatabaseNotificacionAdapter(FirebaseApp app, EstudianteJpaRepository estudiantes) {
        this.estudiantesRef = FirebaseDatabase.getInstance(app).getReference("estudiantes");
        this.estudiantes = estudiantes;
    }

    @Override
    public List<NotificacionResponse> listar(Long userId) {
        String studentKey = studentKey(userId);
        DataSnapshot snapshot = read(estudiantesRef.child(studentKey).child("notificaciones")
                .orderByChild("creadaEnEpoch").limitToLast(50));
        List<NotificacionResponse> result = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            Map<String, Object> fields = child.getValue(new GenericTypeIndicator<Map<String, Object>>() {});
            if (fields == null) continue;
            result.add(new NotificacionResponse(child.getKey(), string(fields, "tipo"),
                    longValue(fields.get("solicitudId")), string(fields, "estado"),
                    string(fields, "mensaje"), Boolean.TRUE.equals(fields.get("leida")),
                    fecha(fields.get("creadaEn"))));
        }
        return result.stream().sorted(Comparator.comparing(NotificacionResponse::creadaEn).reversed()).toList();
    }

    @Override
    public void marcarLeida(Long userId, String notificationId) {
        DatabaseReference ref = estudiantesRef.child(studentKey(userId)).child("notificaciones").child(notificationId);
        DataSnapshot snapshot = read(ref);
        if (!snapshot.exists()) throw new NotificacionNoEncontradaException();
        await(ref.child("leida").setValueAsync(true));
    }

    private String studentKey(Long userId) {
        return estudiantes.findByUsuario_Id(userId).map(s -> s.getCodigoEstudiantil())
                .orElseThrow(() -> new IllegalStateException("El usuario autenticado no tiene código estudiantil."));
    }

    private DataSnapshot read(Query query) {
        CompletableFuture<DataSnapshot> future = new CompletableFuture<>();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) { future.complete(snapshot); }
            public void onCancelled(DatabaseError error) { future.completeExceptionally(error.toException()); }
        });
        return await(future);
    }

    private DataSnapshot read(DatabaseReference ref) { return read((Query) ref); }

    private <T> T await(CompletableFuture<T> future) {
        try {
            return future.get(15, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La operación de Realtime Database fue interrumpida.", ex);
        } catch (ExecutionException | TimeoutException ex) {
            throw new IllegalStateException("No fue posible completar la operación en Realtime Database.", ex);
        }
    }

    private <T> T await(ApiFuture<T> future) {
        try {
            return future.get(15, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La operación de Realtime Database fue interrumpida.", ex);
        } catch (ExecutionException | TimeoutException ex) {
            throw new IllegalStateException("No fue posible completar la operación en Realtime Database.", ex);
        }
    }

    private String string(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }
    private Long longValue(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }
    private OffsetDateTime fecha(Object value) {
        if (value instanceof Number number) return OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(number.longValue()), ZoneOffset.UTC);
        if (value instanceof String text) {
            try { return OffsetDateTime.parse(text); } catch (RuntimeException ignored) { }
        }
        return OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
    }

    public static class NotificacionNoEncontradaException extends RuntimeException { }
}
