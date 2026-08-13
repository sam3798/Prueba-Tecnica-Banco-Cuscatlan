package sv.bancocuscatlan.coworking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sv.bancocuscatlan.coworking.domain.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
}
