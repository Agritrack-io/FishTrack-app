package io.agritrack.epctagcoder.parse.SGTIN;

import static org.junit.Assert.assertEquals;
import static io.agritrack.epctagcoder.parse.SGTIN.ParseSGTIN.Builder;

import org.junit.Test;

import io.agritrack.epctagcoder.exception.EPCParseException;
import io.agritrack.epctagcoder.result.SGTIN;

public class TestParseSGTINWithRfidTag {

    /**
     * we use 2 known CAEN-RT0012 RFID tags, to test the decoder.
     */
    @Test
    public void parseEpcSerialTest() throws EPCParseException {
        String rfidTag1 = "300EFE2F94D01C02540BEBBD";
        String rfidTag2 = "300EFE2F94D01C02540BEBD5";

        SGTIN sgtin1 = Builder().withRFIDTag(rfidTag1).build().getSGTIN();
        SGTIN sgtin2 = Builder().withRFIDTag(rfidTag2).build().getSGTIN();

        System.out.println(String.format("for RFID:%s the SGTIN is:(01)%s %s%s %s (21)%s",
                rfidTag1, sgtin1.getExtensionDigit(), sgtin1.getCompanyPrefix(),
                sgtin1.getItemReference(), sgtin1.getCheckDigit(), sgtin1.getSerial()));
        //System.out.println(String.format("for RFID:%s the serial is:(21)%s", rfidTag1, sgtin1.getSerial()));
        //System.out.println(String.format("for RFID:%s the serial is:(21)%s", rfidTag2, sgtin2.getSerial()));
        assertEquals("10000001981", sgtin1.getSerial());
        assertEquals("10000002005", sgtin2.getSerial());
    }
}
