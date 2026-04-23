package com.cidefenderx.threat;

public class ThreatResult {
    private String ruleName;
    private String threatType;
    private String severity;
    private String title;
    private String description;
    private String mitreTechnique;
    private String attackVector;
    private int riskScore;

    public ThreatResult() {}

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getThreatType() { return threatType; }
    public void setThreatType(String threatType) { this.threatType = threatType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMitreTechnique() { return mitreTechnique; }
    public void setMitreTechnique(String mitreTechnique) { this.mitreTechnique = mitreTechnique; }
    public String getAttackVector() { return attackVector; }
    public void setAttackVector(String attackVector) { this.attackVector = attackVector; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
}
