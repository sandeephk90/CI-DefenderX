package com.cidefenderx.threat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ThreatResult {
    private String ruleName;
    private String threatType;
    private String severity;
    private String title;
    private String description;
    private String mitreTechnique;
    private String attackVector;
    private int riskScore;
}
