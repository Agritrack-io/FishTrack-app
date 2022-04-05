package io.agritrack.ui.login.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class SitesRQ {

    private Double lat;
    private Double lon;
    private Double rad;
    private Integer lvl;

    public SitesRQ(String lat, String lon) {
        this(Double.valueOf(lat), Double.valueOf(lon));
    }


    public SitesRQ(Double lat, Double lon) {
        this.lat = lat;
        this.lon = lon;
        this.rad = 5.0d;
        this.lvl = 3;
    }

    public SitesRQ(Double lat, Double lon, Double rad, Integer lvl) {
        this.lat = lat;
        this.lon = lon;
        this.rad = rad;
        this.lvl = lvl;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getRad() {
        return rad;
    }

    public void setRad(Double rad) {
        this.rad = rad;
    }

    public Integer getLvl() {
        return lvl;
    }

    public void setLvl(Integer lvl) {
        this.lvl = lvl;
    }

    @Override
    public String toString() {
        return String.format("{'lat':%s,'lon':%s,'rad':%s,'lvl':%s}", lat, lon, rad, lvl);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(this, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
