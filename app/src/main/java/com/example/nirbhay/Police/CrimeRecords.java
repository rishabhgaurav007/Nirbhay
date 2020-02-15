package com.example.nirbhay.Police;

public class CrimeRecords {
    private Double latitude, longitude;
    private int threatLevel;
    private String crimeNo;

    public CrimeRecords(){

    }

    public CrimeRecords(Double latitude, Double longitude, int threatLevel, String crimeNo) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.threatLevel = threatLevel;
        this.crimeNo = crimeNo;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public int getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(int threatLevel) {
        this.threatLevel = threatLevel;
    }

    public String getCrimeNo() {
        return crimeNo;
    }

    public void setCrimeNo(String crimeNo) {
        this.crimeNo = crimeNo;
    }
}
