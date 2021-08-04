package io.agritrack.fishtrack.ui.activity.login.api;

import com.google.gson.annotations.SerializedName;

public class SiteInfo {

    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("code")
    private String code;

    @SerializedName("site_lvl")
    private Integer siteLevel;

    @SerializedName("lvl1")
    private String level1;

    @SerializedName("lvl2")
    private String level2;

    @SerializedName("lvl3")
    private String level3;

    @SerializedName("lvl4")
    private String level4;

    @SerializedName("country")
    private String country;

    @SerializedName("region")
    private String region;

    @SerializedName("customer_site_id")
    private String customerSite;

    @SerializedName("lat")
    private Double lat;

    @SerializedName("lon")
    private Double lon;

    @SerializedName("active")
    private Boolean active;


    public SiteInfo() {
    }

    public SiteInfo(Long id, String name, String description, String code,
                    Integer siteLevel, String level1, String level2, String level3,
                    String level4, String country, String region, String customerSite,
                    Double lat, Double lon, Boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.code = code;
        this.siteLevel = siteLevel;
        this.level1 = level1;
        this.level2 = level2;
        this.level3 = level3;
        this.level4 = level4;
        this.country = country;
        this.region = region;
        this.customerSite = customerSite;
        this.lat = lat;
        this.lon = lon;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getSiteLevel() {
        return siteLevel;
    }

    public void setSiteLevel(Integer siteLevel) {
        this.siteLevel = siteLevel;
    }

    public String getLevel1() {
        return level1;
    }

    public void setLevel1(String level1) {
        this.level1 = level1;
    }

    public String getLevel2() {
        return level2;
    }

    public void setLevel2(String level2) {
        this.level2 = level2;
    }

    public String getLevel3() {
        return level3;
    }

    public void setLevel3(String level3) {
        this.level3 = level3;
    }

    public String getLevel4() {
        return level4;
    }

    public void setLevel4(String level4) {
        this.level4 = level4;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCustomerSite() {
        return customerSite;
    }

    public void setCustomerSite(String customerSite) {
        this.customerSite = customerSite;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
