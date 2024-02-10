package io.agritrack.kefalonia.data.dto.common;

import io.agritrack.kefalonia.data.model.common.Species;

public class SpeciesDTO {

    public Long id;
    public String country;
    public String sc_name;
    public String local_name;
    public String name;
    public String type;

    public static Species convert(SpeciesDTO speciesDTO) {
        Species species = new Species();
        species.id = speciesDTO.id;
        species.country = speciesDTO.country;
        species.scientificName = speciesDTO.sc_name;
        species.localName = speciesDTO.local_name;
        species.name = speciesDTO.name;
        species.type = speciesDTO.type;
        return species;
    }
}
