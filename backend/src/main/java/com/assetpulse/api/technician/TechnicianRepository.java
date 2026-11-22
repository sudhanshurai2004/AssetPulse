package com.assetpulse.api.technician;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnicianRepository extends JpaRepository<Technician, Long> {
    Optional<Technician> findFirstByActiveTrueOrderByIdAsc();
    boolean existsByEmail(String email);
}
