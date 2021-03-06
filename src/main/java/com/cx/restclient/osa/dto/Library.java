package com.cx.restclient.osa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by zoharby on 09/01/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Library implements Serializable {

    private String id;//:"36b32b00-9ee6-4e2f-85c9-3f03f26519a9",
    private String name;//:"lib-name",
    private String version;//:"lib-version",
    private int highVulnerabilityCount;//:1,
    private int mediumVulnerabilityCount;//:1,
    private int lowVulnerabilityCount;//:1,
    private String newestVersion;//:"1.0.0",
    private String newestVersionReleaseDate;//:"2016-12-19T10:16:19.1206743Z",
    private int numberOfVersionsSinceLastUpdate;//":10,
    private int confidenceLevel;//":100


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getHighVulnerabilityCount() {
        return highVulnerabilityCount;
    }

    public void setHighVulnerabilityCount(int highVulnerabilityCount) {
        this.highVulnerabilityCount = highVulnerabilityCount;
    }

    public int getMediumVulnerabilityCount() {
        return mediumVulnerabilityCount;
    }

    public void setMediumVulnerabilityCount(int mediumVulnerabilityCount) {
        this.mediumVulnerabilityCount = mediumVulnerabilityCount;
    }

    public int getLowVulnerabilityCount() {
        return lowVulnerabilityCount;
    }

    public void setLowVulnerabilityCount(int lowVulnerabilityCount) {
        this.lowVulnerabilityCount = lowVulnerabilityCount;
    }

    public String getNewestVersion() {
        return newestVersion;
    }

    public void setNewestVersion(String newestVersion) {
        this.newestVersion = newestVersion;
    }

    public String getNewestVersionReleaseDate() {
        return newestVersionReleaseDate;
    }

    public void setNewestVersionReleaseDate(String newestVersionReleaseDate) {
        this.newestVersionReleaseDate = newestVersionReleaseDate;
    }

    public int getNumberOfVersionsSinceLastUpdate() {
        return numberOfVersionsSinceLastUpdate;
    }

    public void setNumberOfVersionsSinceLastUpdate(int numberOfVersionsSinceLastUpdate) {
        this.numberOfVersionsSinceLastUpdate = numberOfVersionsSinceLastUpdate;
    }

    public int getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(int confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

}
