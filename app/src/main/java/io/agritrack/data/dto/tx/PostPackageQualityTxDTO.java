package io.agritrack.data.dto.tx;

import io.agritrack.data.model.tx.PostPackageQualityTransaction;

public class PostPackageQualityTxDTO {

    public Long sample_date;
    public String plot;
    public String box_sn;
    public Double tempT1;
    public Double tempT2;
    public Double tempT3;
    public String site;
    public String user;
    public Double longitude;
    public Double latitude;

    public static PostPackageQualityTxDTO convert(PostPackageQualityTransaction quality) {
        PostPackageQualityTxDTO qualityTxDto = new PostPackageQualityTxDTO();

        qualityTxDto.plot = quality.plot;
        qualityTxDto.box_sn = quality.boxSn;
        qualityTxDto.tempT1 = quality.tempT1;
        qualityTxDto.tempT2 = quality.tempT2;
        qualityTxDto.tempT3 = quality.tempT3;
        qualityTxDto.site = quality.site;
        qualityTxDto.user = quality.user;
        qualityTxDto.sample_date = quality.sampleDate;
        qualityTxDto.longitude = quality.longitude;
        qualityTxDto.latitude = quality.latitude;

        return qualityTxDto;
    }
}
