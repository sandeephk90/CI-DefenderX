package com.cidefenderx.service;

import com.cidefenderx.dto.DashboardSummaryDto;
import com.cidefenderx.model.Endpoint;
import com.cidefenderx.model.Threat;
import com.cidefenderx.repository.EndpointRepository;
import com.cidefenderx.repository.EventRepository;
import com.cidefenderx.repository.ThreatRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final EndpointRepository endpointRepository;
    private final ThreatRepository threatRepository;
    private final EventRepository eventRepository;

    public DashboardService(EndpointRepository endpointRepository,
                            ThreatRepository threatRepository,
                            EventRepository eventRepository) {
        this.endpointRepository = endpointRepository;
        this.threatRepository = threatRepository;
        this.eventRepository = eventRepository;
    }

    public DashboardSummaryDto getSummary() {
        long totalEndpoints    = endpointRepository.count();
        long onlineEndpoints   = endpointRepository.countByStatus(Endpoint.EndpointStatus.ONLINE);
        long isolatedEndpoints = endpointRepository.countByStatus(Endpoint.EndpointStatus.ISOLATED);
        long openThreats       = threatRepository.countByStatus(Threat.ThreatStatus.OPEN);
        long criticalThreats   = threatRepository.countBySeverityAndStatus(Threat.Severity.CRITICAL, Threat.ThreatStatus.OPEN);
        long highThreats       = threatRepository.countBySeverityAndStatus(Threat.Severity.HIGH, Threat.ThreatStatus.OPEN);

        Map<String, Long> severityDist = new LinkedHashMap<>();
        threatRepository.countOpenThreatsBySeverity()
                .forEach(row -> severityDist.put(((Enum<?>) row[0]).name(), (Long) row[1]));

        Map<String, Long> typeDist = new LinkedHashMap<>();
        threatRepository.countByThreatType().stream().limit(10)
                .forEach(row -> typeDist.put((String) row[0], (Long) row[1]));

        Map<String, Long> osDist = new LinkedHashMap<>();
        endpointRepository.countByOsType()
                .forEach(row -> osDist.put(((Enum<?>) row[0]).name(), (Long) row[1]));

        List<DashboardSummaryDto.TrendPoint> trend = threatRepository
                .dailyThreatCount(OffsetDateTime.now().minusDays(7))
                .stream()
                .map(row -> new DashboardSummaryDto.TrendPoint(row[0].toString(), ((Number) row[1]).longValue()))
                .collect(Collectors.toList());

        long highRiskCount = endpointRepository.findHighRiskEndpoints(70).size();

        DashboardSummaryDto dto = new DashboardSummaryDto();
        dto.setTotalEndpoints(totalEndpoints);
        dto.setOnlineEndpoints(onlineEndpoints);
        dto.setIsolatedEndpoints(isolatedEndpoints);
        dto.setOpenThreats(openThreats);
        dto.setCriticalThreats(criticalThreats);
        dto.setHighThreats(highThreats);
        dto.setHighRiskEndpointCount(highRiskCount);
        dto.setSeverityDistribution(severityDist);
        dto.setThreatTypeDistribution(typeDist);
        dto.setOsDistribution(osDist);
        dto.setThreatTrend(trend);
        return dto;
    }
}
