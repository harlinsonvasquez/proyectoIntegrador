-- V3: backfill seguro para matrículas que ya tienen todas sus notas parciales.
-- Solo reemplaza nota_final cuando las evaluaciones activas suman 100 % y
-- existe una nota para cada evaluación. No toca calificaciones incompletas.
WITH porcentajes AS (
    SELECT oferta_asignatura_id, SUM(porcentaje) AS porcentaje_total, COUNT(*) AS evaluaciones_activas
      FROM gestion_estudiantil.evaluacion_asignatura
     WHERE activa = TRUE
     GROUP BY oferta_asignatura_id
), resultados AS (
    SELECT np.matricula_id,
           np.oferta_asignatura_id,
           SUM(np.nota * ea.porcentaje / 100.0) AS nota_calculada,
           COUNT(*) AS notas_registradas
      FROM gestion_estudiantil.nota_parcial np
      JOIN gestion_estudiantil.evaluacion_asignatura ea
        ON ea.id = np.evaluacion_id
       AND ea.oferta_asignatura_id = np.oferta_asignatura_id
       AND ea.activa = TRUE
     GROUP BY np.matricula_id, np.oferta_asignatura_id
), completas AS (
    SELECT r.matricula_id, ROUND(r.nota_calculada, 2) AS nota_final
      FROM resultados r
      JOIN porcentajes p ON p.oferta_asignatura_id = r.oferta_asignatura_id
     WHERE p.porcentaje_total = 100
       AND r.notas_registradas = p.evaluaciones_activas
)
UPDATE gestion_estudiantil.matricula_asignatura m
   SET nota_final = c.nota_final
  FROM completas c
 WHERE m.id = c.matricula_id
   AND m.nota_final IS DISTINCT FROM c.nota_final;
