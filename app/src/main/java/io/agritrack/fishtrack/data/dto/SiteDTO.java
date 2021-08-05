package io.agritrack.fishtrack.data.dto;

import io.agritrack.fishtrack.data.model.Site;

public class SiteDTO {
    public Long id;
    public String code;
    public String name;
    public String description;
    public String siteLevel;
    public Boolean active;

    public static Site convert(SiteDTO siteDTO) {
        Site site = new Site();
        site.id = siteDTO.id;
        site.code = siteDTO.code;
        site.name = siteDTO.name;
        site.description = siteDTO.description;
        site.siteLevel = siteDTO.siteLevel;
        site.active = siteDTO.active;
        return site;
    }
}
