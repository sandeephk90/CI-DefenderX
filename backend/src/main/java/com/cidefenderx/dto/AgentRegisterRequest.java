package com.cidefenderx.dto;

import jakarta.validation.constraints.NotBlank;

public class AgentRegisterRequest {
    @NotBlank private String agentId;
    @NotBlank private String hostname;
    private String ipAddress;
    private String osType;
    private String osVersion;
    private String version;

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getOsType() { return osType; }
    public void setOsType(String osType) { this.osType = osType; }
    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
