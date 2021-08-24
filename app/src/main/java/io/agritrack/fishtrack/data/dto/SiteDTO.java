package io.agritrack.fishtrack.data.dto;

import io.agritrack.fishtrack.data.model.Site;

public class SiteDTO {
    public Long id;
    public String code;
    public String site_type;
    public String name;
    public String description;
    public Integer site_lvl;
    public Boolean active;
    public String lvl1;
    public String lvl2;
    public String lvl3;
    public String lvl4;
    public String country;
    public String region;
    public String customer_site_id;

    public static Site convert(SiteDTO siteDTO) {
        Site site = new Site();
        site.id = siteDTO.id;
        site.code = siteDTO.code;
        site.siteType = siteDTO.site_type;
        site.name = siteDTO.name;
        site.description = siteDTO.description;
        site.siteLevel = siteDTO.site_lvl;
        site.lvl1 = siteDTO.lvl1;
        site.lvl2 = siteDTO.lvl2;
        site.lvl3 = siteDTO.lvl3;
        site.lvl4 = siteDTO.lvl4;
        site.country = siteDTO.country;
        site.region = siteDTO.region;
        site.customer_site_id = siteDTO.customer_site_id;
        site.active = siteDTO.active;
        return site;
    }
}
