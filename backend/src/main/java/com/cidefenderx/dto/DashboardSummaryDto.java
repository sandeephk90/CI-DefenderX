package com.cidefenderx.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardSummaryDto {
    private long totalEndpoints;
    private long onlineEndpoints;
    private long isolatedEndpoints;
    private long openThreats;
    private long criticalThreats;
    private long highThreats;
    private long highRiskEndpointCount;
    private Map<String, Long> severityDistribution;
    private Map<String, Long> threatTypeDistribution;
    private Map<String, Long> osDistribution;
    private List<TrendPoint> threatTrend;

    @Data
    public static class TrendPoint {
        private final String date;
        private final long count;
    }
}
