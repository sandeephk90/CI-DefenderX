package com.cidefenderx.service;

import com.cidefenderx.dto.DashboardSummaryDto;
import com.cidefenderx.model.Endpoint;
import com.cidefenderx.model.Threat;
import com.cidefenderx.repository.EndpointRepository;
import com.cidefenderx.repository.EventRepository;
import com.cidefenderx.repository.ThreatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EndpointRepository endpointRepository;
    private final ThreatRepository threatRepository;
    private final EventRepository eventRepository;

    public DashboardSummaryDto getSummary() {
        long totalEndpoints = endpointRepository.count();
        long onlineEndpoints = endpointRepository.countByStatus(Endpoint.EndpointStatus.ONLINE);
        long isolatedEndpoints = endpointRepository.countByStatus(Endpoint.EndpointStatus.ISOLATED);

        long openThreats = threatRepository.countByStatus(Threat.ThreatStatus.OPEN);
        long criticalThreats = threatRepository.countBySeverityAndStatus(
                Threat.Severity.CRITICAL, Threat.ThreatStatus.OPEN);
        long highThreats = threatRepository.countBySeverityAndStatus(
                Threat.Severity.HIGH, Threat.ThreatStatus.OPEN);

        List<Threat> recentThreats = threatRepository
                .findRecentThreats(OffsetDateTime.now().minusHours(24));

        // Severity distribution
        Map<String, Long> severityDist = new LinkedHashMap<>();
        threatRepository.countOpenThreatsBySeverity().forEach(row ->
                severityDist.put(((Enum<?>) row[0]).name(), (Long) row[1]));

        // Threat type distribution
        Map<String, Long> typeDist = new LinkedHashMap<>();
        threatRepository.countByThreatType().stream().limit(10).forEach(row ->
                typeDist.put((String) row[0], (Long) row[1]));

        // OS distribution
        Map<String, Long> osDist = new LinkedHashMap<>();
        endpointRepository.countByOsType().forEach(row ->
                osDist.put(((Enum<?>) row[0]).name(), (Long) row[1]));

        // 7-day threat trend
        List<DashboardSummaryDto.TrendPoint> trend = threatRepository
                .dailyThreatCount(OffsetDateTime.now().minusDays(7))
                .stream()
                .map(row -> new DashboardSummaryDto.TrendPoint(
                        row[0].toString(), ((Number) row[1]).longValue()))
                .collect(Collectors.toList());

        // High risk endpoints
        List<Endpoint> highRisk = endpointRepository.findHighRiskEndpoints(70);

        return DashboardSummaryDto.builder()
                .totalEndpoints(totalEndpoints)
                .onlineEndpoints(onlineEndpoints)
                .isolatedEndpoints(isolatedEndpoints)
                .openThreats(openThreats)
                .criticalThreats(criticalThreats)
                .highThreats(highThreats)
                .severityDistribution(severityDist)
                .threatTypeDistribution(typeDist)
                .osDistribution(osDist)
                .threatTrend(trend)
                .highRiskEndpointCount((long) highRisk.size())
                .build();
    }
}
