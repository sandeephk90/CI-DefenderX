package com.cidefenderx.controller;

import com.cidefenderx.model.Endpoint;
import com.cidefenderx.repository.EndpointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/endpoints")
@RequiredArgsConstructor
public class EndpointController {

    private final EndpointRepository endpointRepository;

    @GetMapping
    public ResponseEntity<Page<Endpoint>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("riskScore").descending());
        Page<Endpoint> result;
        if (status != null) {
            result = new PageImpl<>(
                    endpointRepository.findByStatus(Endpoint.EndpointStatus.valueOf(status.toUpperCase())),
                    pageable, 0);
        } else {
            result = endpointRepository.findAll(pageable);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Endpoint> get(@PathVariable UUID id) {
        return endpointRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/isolate")
    public ResponseEntity<?> isolate(@PathVariable UUID id) {
        return endpointRepository.findById(id).map(ep -> {
            ep.setStatus(Endpoint.EndpointStatus.ISOLATED);
            endpointRepository.save(ep);
            return ResponseEntity.ok(Map.of("status", "isolated"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<?> restore(@PathVariable UUID id) {
        return endpointRepository.findById(id).map(ep -> {
            ep.setStatus(Endpoint.EndpointStatus.ONLINE);
            endpointRepository.save(ep);
            return ResponseEntity.ok(Map.of("status", "restored"));
        }).orElse(ResponseEntity.notFound().build());
    }
}
