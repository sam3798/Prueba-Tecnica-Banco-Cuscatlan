package sv.bancocuscatlan.coworking.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sv.bancocuscatlan.coworking.domain.Reserva;
import sv.bancocuscatlan.coworking.domain.ReservationStatus;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END
            FROM Reserva r
            WHERE r.espacio.id = :espacioId
              AND r.estado <> sv.bancocuscatlan.coworking.domain.ReservationStatus.CANCELLED
              AND r.inicio < :fin
              AND r.fin > :inicio
            """)
    boolean existsOverlap(
            @Param("espacioId") Long espacioId,
            @Param("inicio") Instant inicio,
            @Param("fin") Instant fin);

    @Query("""
            SELECT r FROM Reserva r
            JOIN FETCH r.espacio
            JOIN FETCH r.usuario
            WHERE r.usuario.id = :usuarioId
            ORDER BY r.inicio DESC
            """)
    List<Reserva> findAllByUsuarioIdWithDetails(@Param("usuarioId") Long usuarioId);

    @Query("""
            SELECT r FROM Reserva r
            JOIN FETCH r.espacio
            JOIN FETCH r.usuario
            ORDER BY r.inicio DESC
            """)
    List<Reserva> findAllWithDetails();

    @Query("""
            SELECT r FROM Reserva r
            JOIN FETCH r.espacio
            JOIN FETCH r.usuario
            WHERE r.id = :id
            """)
    Optional<Reserva> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT r FROM Reserva r
            JOIN FETCH r.espacio
            WHERE r.estado IN :estados
              AND r.inicio < :hasta
              AND r.fin > :desde
            """)
    List<Reserva> findForOccupancy(
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta,
            @Param("estados") List<ReservationStatus> estados);
}
