package io.agritrack.fishtrack.data.dto.common;

import io.agritrack.fishtrack.data.model.common.Reader;

public class ReaderDTO {

    public Long id;
    public String type;
    public String serialNumber;
    public String ipAddress;
    public String name;
    public String code;
    public Long givenAt;
    public String deliveryNote;
    public Long lastSync;

    public static Reader convert(ReaderDTO readerDTO) {
        Reader reader = new Reader();
        reader.id = readerDTO.id;
        reader.serialNumber = readerDTO.serialNumber;
        reader.ipAddress = readerDTO.ipAddress;
        reader.name = readerDTO.name;
        reader.code = readerDTO.code;
        reader.givenAt = readerDTO.givenAt;
        reader.deliveryNote = readerDTO.deliveryNote;
        reader.lastSync = readerDTO.lastSync;
        return reader;
    }
}
