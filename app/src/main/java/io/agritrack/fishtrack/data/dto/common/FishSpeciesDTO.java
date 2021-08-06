package io.agritrack.fishtrack.data.dto.common;

import io.agritrack.fishtrack.data.model.common.FishSpecies;

public class FishSpeciesDTO {

    public Long id;
    public String country;
    public String scientificName;
    public String localName;
    public String name;

    public static FishSpecies convert(FishSpeciesDTO fishSpeciesDTO) {
        FishSpecies fishSpecies = new FishSpecies();
        fishSpecies.id = fishSpeciesDTO.id;
        fishSpecies.country = fishSpeciesDTO.country;
        fishSpecies.scientificName = fishSpeciesDTO.scientificName;
        fishSpecies.localName = fishSpeciesDTO.localName;
        fishSpecies.name = fishSpeciesDTO.name;
        return fishSpecies;
    }
}
