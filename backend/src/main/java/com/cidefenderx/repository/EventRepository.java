package com.cidefenderx.repository;

import com.cidefenderx.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByEndpointId(UUID endpointId, Pageable pageable);

    List<Event> findByEndpointIdAndOccurredAtAfter(UUID endpointId, OffsetDateTime since);

    Page<Event> findBySeverity(Event.Severity severity, Pageable pageable);

    long countByEndpointIdAndOccurredAtAfter(UUID endpointId, OffsetDateTime since);
}
