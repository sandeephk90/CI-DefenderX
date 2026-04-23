package com.cidefenderx.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgentRegisterRequest {
    @NotBlank
    private String agentId;
    @NotBlank
    private String hostname;
    private String ipAddress;
    private String osType;
    private String osVersion;
    private String version;
}
