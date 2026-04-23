package com.cidefenderx.repository;

import com.cidefenderx.model.Threat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ThreatRepository extends JpaRepository<Threat, UUID> {

    Page<Threat> findByEndpointId(UUID endpointId, Pageable pageable);

    Page<Threat> findBySeverity(Threat.Severity severity, Pageable pageable);

    Page<Threat> findByStatus(Threat.ThreatStatus status, Pageable pageable);

    List<Threat> findByEndpointIdAndStatus(UUID endpointId, Threat.ThreatStatus status);

    @Query("SELECT t FROM Threat t WHERE t.detectedAt >= :since ORDER BY t.detectedAt DESC")
    List<Threat> findRecentThreats(OffsetDateTime since);

    @Query("SELECT t.severity, COUNT(t) FROM Threat t WHERE t.status = 'OPEN' GROUP BY t.severity")
    List<Object[]> countOpenThreatsBySeverity();

    @Query("SELECT t.threatType, COUNT(t) FROM Threat t GROUP BY t.threatType ORDER BY COUNT(t) DESC")
    List<Object[]> countByThreatType();

    long countByStatus(Threat.ThreatStatus status);

    long countBySeverityAndStatus(Threat.Severity severity, Threat.ThreatStatus status);

    @Query("SELECT DATE(t.detectedAt), COUNT(t) FROM Threat t WHERE t.detectedAt >= :since GROUP BY DATE(t.detectedAt) ORDER BY DATE(t.detectedAt)")
    List<Object[]> dailyThreatCount(OffsetDateTime since);
}
