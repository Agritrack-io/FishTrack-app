package io.agritrack.data.dto.common;

import io.agritrack.data.model.common.FishSpecies;

public class FishSpeciesDTO {

    public Long id;
    public String country;
    public String sc_name;
    public String local_name;
    public String name;

    public static FishSpecies convert(FishSpeciesDTO fishSpeciesDTO) {
        FishSpecies fishSpecies = new FishSpecies();
        fishSpecies.id = fishSpeciesDTO.id;
        fishSpecies.country = fishSpeciesDTO.country;
        fishSpecies.scientificName = fishSpeciesDTO.sc_name;
        fishSpecies.localName = fishSpeciesDTO.local_name;
        fishSpecies.name = fishSpeciesDTO.name;
        return fishSpecies;
    }
}
