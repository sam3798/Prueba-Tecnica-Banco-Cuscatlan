package sv.bancocuscatlan.coworking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import sv.bancocuscatlan.coworking.domain.Espacio;

public interface EspacioRepository extends JpaRepository<Espacio, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Espacio e WHERE e.id = :id")
    Optional<Espacio> findByIdForUpdate(@Param("id") Long id);
}
