package io.agritrack.philosofish.data.dto.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.agritrack.philosofish.data.model.tx.FinalQualityTransaction;
import io.agritrack.philosofish.data.model.tx.QualityTransaction;
import io.agritrack.philosofish.data.model.tx.TransportTransaction;

public class MediaDTO {

    public String lot;
    public String photo;

    public MediaDTO(String lot, String photo) {
        this.lot = lot;
        this.photo = photo;
    }

    public static MediaDTO convert(TransportTransaction transport) {
        return new MediaDTO(transport.destination, transport.driverSignature);
    }

    public static MediaDTO convert(FinalQualityTransaction quality) {
        return new MediaDTO(quality.lot, quality.signature);
        //return new MediaDTO(quality.qualityBins, quality.driverSignature);
    }
}
