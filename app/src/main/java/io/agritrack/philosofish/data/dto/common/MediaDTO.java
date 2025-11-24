package io.agritrack.philosofish.data.dto.common;

import android.util.Base64;

import io.agritrack.philosofish.data.model.tx.FinalQualityTransaction;
import io.agritrack.philosofish.data.model.tx.TransportTransaction;

public class MediaDTO {

    public String lot;
    public String photo;

    public MediaDTO(String lot, String photo) {
        this.lot = lot;
        this.photo = photo;
    }

    // TRANSPORT
    public static MediaDTO convert(TransportTransaction transport) {
        return new MediaDTO(
                transport.destination,        // must match backend
                transport.driverSignature     // already base64
        );
    }

    // QUALITY — FIXED
    public static MediaDTO convert(FinalQualityTransaction quality) {

        String base64 = null;

        // always use raw bytes if present
        if (quality.signatureBytes != null && quality.signatureBytes.length > 0) {
            base64 = Base64.encodeToString(quality.signatureBytes, Base64.NO_WRAP);
        }

        return new MediaDTO(
                quality.fishingLot,   // <-- <-- sending fishingLot here
                base64                 // raw Base64 PNG
        );
    }

}
