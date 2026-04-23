package com.cidefenderx.controller;

import com.cidefenderx.dto.AgentRegisterRequest;
import com.cidefenderx.dto.TelemetryPayload;
import com.cidefenderx.model.Endpoint;
import com.cidefenderx.repository.EndpointRepository;
import com.cidefenderx.service.TelemetryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class AgentController {

    private final EndpointRepository endpointRepository;
    private final TelemetryService telemetryService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AgentRegisterRequest req) {
        Endpoint endpoint = endpointRepository.findByAgentId(req.getAgentId())
                .orElse(Endpoint.builder()
                        .agentId(req.getAgentId())
                        .registeredAt(OffsetDateTime.now())
                        .build());

        endpoint.setHostname(req.getHostname());
        endpoint.setIpAddress(req.getIpAddress());
        endpoint.setOsType(parseOsType(req.getOsType()));
        endpoint.setAgentVersion(req.getVersion());
        endpoint.setStatus(Endpoint.EndpointStatus.ONLINE);
        endpoint.setLastSeen(OffsetDateTime.now());
        endpointRepository.save(endpoint);

        log.info("Agent registered: {} ({})", req.getAgentId(), req.getHostname());
        return ResponseEntity.ok(Map.of(
                "status", "registered",
                "endpointId", endpoint.getId().toString()
        ));
    }

    @PostMapping("/heartbeat/{agentId}")
    public ResponseEntity<?> heartbeat(@PathVariable String agentId) {
        endpointRepository.findByAgentId(agentId).ifPresent(ep -> {
            ep.setLastSeen(OffsetDateTime.now());
            ep.setStatus(Endpoint.EndpointStatus.ONLINE);
            endpointRepository.save(ep);
        });
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/telemetry")
    public ResponseEntity<?> ingestTelemetry(@RequestBody TelemetryPayload payload) {
        telemetryService.process(payload);
        return ResponseEntity.accepted().body(Map.of("status", "accepted"));
    }

    private Endpoint.OsType parseOsType(String osType) {
        if (osType == null) return Endpoint.OsType.UNKNOWN;
        return switch (osType.toLowerCase()) {
            case "windows" -> Endpoint.OsType.WINDOWS;
            case "linux" -> Endpoint.OsType.LINUX;
            case "darwin", "macos" -> Endpoint.OsType.MACOS;
            default -> Endpoint.OsType.UNKNOWN;
        };
    }
}
