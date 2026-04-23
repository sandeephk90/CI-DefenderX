package com.cidefenderx.controller;

import com.cidefenderx.model.Threat;
import com.cidefenderx.repository.ThreatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/threats")
@RequiredArgsConstructor
public class ThreatController {

    private final ThreatRepository threatRepository;

    @GetMapping
    public ResponseEntity<Page<Threat>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("detectedAt").descending());

        if (severity != null) {
            return ResponseEntity.ok(threatRepository.findBySeverity(
                    Threat.Severity.valueOf(severity.toUpperCase()), pageable));
        }
        if (status != null) {
            return ResponseEntity.ok(threatRepository.findByStatus(
                    Threat.ThreatStatus.valueOf(status.toUpperCase()), pageable));
        }
        return ResponseEntity.ok(threatRepository.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Threat> get(@PathVariable UUID id) {
        return threatRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body
    ) {
        return threatRepository.findById(id).map(t -> {
            t.setStatus(Threat.ThreatStatus.valueOf(body.get("status").toUpperCase()));
            if (body.containsKey("analystNotes")) {
                t.setAnalystNotes(body.get("analystNotes"));
            }
            threatRepository.save(t);
            return ResponseEntity.ok(Map.of("updated", true));
        }).orElse(ResponseEntity.notFound().build());
    }
}
