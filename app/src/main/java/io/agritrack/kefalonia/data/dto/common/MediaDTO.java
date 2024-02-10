package io.agritrack.kefalonia.data.dto.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.agritrack.kefalonia.data.model.tx.QualityTransaction;
import io.agritrack.kefalonia.data.model.tx.TransportTransaction;

public class MediaDTO {

    public Set<String> bins_loaded;
    public String photo;

    public MediaDTO(List<String> bins, String photo) {
        this.bins_loaded = new HashSet<>(bins);
        this.photo = photo;
    }

    public static MediaDTO convert(TransportTransaction transport) {
        return new MediaDTO(transport.loadedBins, transport.driverSignature);
    }

    public static MediaDTO convert(QualityTransaction quality) {
        return null;
        //return new MediaDTO(quality.qualityBins, quality.driverSignature);
    }
}
