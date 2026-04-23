package com.cidefenderx.repository;

import com.cidefenderx.model.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EndpointRepository extends JpaRepository<Endpoint, UUID> {

    Optional<Endpoint> findByAgentId(String agentId);

    List<Endpoint> findByStatus(Endpoint.EndpointStatus status);

    List<Endpoint> findByOsType(Endpoint.OsType osType);

    @Query("SELECT e FROM Endpoint e WHERE e.riskScore >= :minScore ORDER BY e.riskScore DESC")
    List<Endpoint> findHighRiskEndpoints(int minScore);

    @Query("SELECT COUNT(e) FROM Endpoint e WHERE e.status = :status")
    long countByStatus(Endpoint.EndpointStatus status);

    @Query("SELECT e.osType, COUNT(e) FROM Endpoint e GROUP BY e.osType")
    List<Object[]> countByOsType();
}
