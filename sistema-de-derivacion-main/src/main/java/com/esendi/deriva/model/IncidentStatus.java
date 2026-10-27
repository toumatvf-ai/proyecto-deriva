package com.esendi.deriva.model;

import jakarta.persistence.*;

@Entity
@Table(name = "incident_status")
public class IncidentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "incident_status_id")
    private Integer incidentStatusId;

    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public IncidentStatus() {}

    public Integer getIncidentStatusId() { return incidentStatusId; }
    public void setIncidentStatusId(Integer incidentStatusId) { this.incidentStatusId = incidentStatusId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}