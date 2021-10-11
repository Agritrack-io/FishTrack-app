package io.agritrack.data.dto.common;

import io.agritrack.data.model.common.Reader;

public class ReaderDTO {

    public Long id;
    public String type;
    public String serial_number;
    public String ip;
    public String name;
    public String code;
    public Long date_given;
    public String delivery_note;
    public Long last_sync;

    public static Reader convert(ReaderDTO readerDTO) {
        Reader reader = new Reader();
        reader.id = readerDTO.id;
        reader.serialNumber = readerDTO.serial_number;
        reader.ipAddress = readerDTO.ip;
        reader.name = readerDTO.name;
        reader.code = readerDTO.code;
        reader.givenAt = readerDTO.date_given;
        reader.deliveryNote = readerDTO.delivery_note;
        reader.lastSync = readerDTO.last_sync;
        return reader;
    }
}
