package io.agritrack.kefalonia.data.dto;

import io.agritrack.kefalonia.data.model.EncodingSchemeEntity;

public class EncodingSchemeDTO {

    public String code;
    public String description;
    public String category;
    public Integer encoding_index;
    public String tagPrefix;
    public Integer tagLength;


    public static EncodingSchemeEntity convert(EncodingSchemeDTO schemeDTO) {
        EncodingSchemeEntity schemeEntity = new EncodingSchemeEntity();
        schemeEntity.code = schemeDTO.code;
        schemeEntity.description = schemeDTO.description;
        schemeEntity.category = schemeDTO.category;
        schemeEntity.encoding_index = schemeDTO.encoding_index;
        return schemeEntity;
    }
}
