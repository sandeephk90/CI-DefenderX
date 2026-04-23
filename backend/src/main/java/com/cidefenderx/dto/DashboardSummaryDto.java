package com.cidefenderx.dto;

import java.util.List;
import java.util.Map;

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

    public static class TrendPoint {
        private String date;
        private long count;
        public TrendPoint() {}
        public TrendPoint(String date, long count) { this.date = date; this.count = count; }
        public String getDate() { return date; }
        public long getCount() { return count; }
    }

    public long getTotalEndpoints() { return totalEndpoints; }
    public void setTotalEndpoints(long v) { this.totalEndpoints = v; }
    public long getOnlineEndpoints() { return onlineEndpoints; }
    public void setOnlineEndpoints(long v) { this.onlineEndpoints = v; }
    public long getIsolatedEndpoints() { return isolatedEndpoints; }
    public void setIsolatedEndpoints(long v) { this.isolatedEndpoints = v; }
    public long getOpenThreats() { return openThreats; }
    public void setOpenThreats(long v) { this.openThreats = v; }
    public long getCriticalThreats() { return criticalThreats; }
    public void setCriticalThreats(long v) { this.criticalThreats = v; }
    public long getHighThreats() { return highThreats; }
    public void setHighThreats(long v) { this.highThreats = v; }
    public long getHighRiskEndpointCount() { return highRiskEndpointCount; }
    public void setHighRiskEndpointCount(long v) { this.highRiskEndpointCount = v; }
    public Map<String, Long> getSeverityDistribution() { return severityDistribution; }
    public void setSeverityDistribution(Map<String, Long> v) { this.severityDistribution = v; }
    public Map<String, Long> getThreatTypeDistribution() { return threatTypeDistribution; }
    public void setThreatTypeDistribution(Map<String, Long> v) { this.threatTypeDistribution = v; }
    public Map<String, Long> getOsDistribution() { return osDistribution; }
    public void setOsDistribution(Map<String, Long> v) { this.osDistribution = v; }
    public List<TrendPoint> getThreatTrend() { return threatTrend; }
    public void setThreatTrend(List<TrendPoint> v) { this.threatTrend = v; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final DashboardSummaryDto d = new DashboardSummaryDto();
        public Builder totalEndpoints(long v) { d.totalEndpoints = v; return this; }
        public Builder onlineEndpoints(long v) { d.onlineEndpoints = v; return this; }
        public Builder isolatedEndpoints(long v) { d.isolatedEndpoints = v; return this; }
        public Builder openThreats(long v) { d.openThreats = v; return this; }
        public Builder criticalThreats(long v) { d.criticalThreats = v; return this; }
        public Builder highThreats(long v) { d.highThreats = v; return this; }
        public Builder highRiskEndpointCount(long v) { d.highRiskEndpointCount = v; return this; }
        public Builder severityDistribution(Map<String, Long> v) { d.severityDistribution = v; return this; }
        public Builder threatTypeDistribution(Map<String, Long> v) { d.threatTypeDistribution = v; return this; }
        public Builder osDistribution(Map<String, Long> v) { d.osDistribution = v; return this; }
        public Builder threatTrend(List<TrendPoint> v) { d.threatTrend = v; return this; }
        public DashboardSummaryDto build() { return d; }
    }
}
