package io.agritrack.rfid;

import static io.agritrack.common.Filters.RFID_BIN;
import static io.agritrack.common.Filters.RFID_CAGE;

import com.android.hdhe.uhf.reader.UhfReader;
import com.android.hdhe.uhf.readerInterface.TagModel;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Stream;

import cn.pda.serialport.Tools;

public class SingleShotScanner implements Callable<String> {
    private final Function<TagModel, String> TagToString = t -> Tools.Bytes2HexString(t.getmEpcBytes(), t.getmEpcBytes().length);
    private UhfReader uhfReader;
    private String RFID_FILTER = null;
    private Boolean trimEPCFlag = Boolean.TRUE;

    public void setUhfReader(UhfReader uhfReader) {
        this.uhfReader = uhfReader;
        this.uhfReader.setOutputPower(33);
    }

    public void setFilter(String rfidFilter) {
        this.RFID_FILTER = rfidFilter;
        this.trimEPCFlag = (RFID_BIN.equalsIgnoreCase(rfidFilter)) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public String call() throws Exception {
        int idx = 0;
        while (true) {
            idx++;
            if (uhfReader != null) {
                final List<TagModel> tagList = uhfReader.inventoryRealTime();
                if (tagList != null && !tagList.isEmpty()) {
                    Stream<TagModel> filteredStream = tagList.stream().filter(f -> this.RFID_FILTER == null || (TagToString.apply(f)).indexOf(this.RFID_FILTER) == 11);
                    Optional<TagModel> tag = filteredStream.sorted((y, x) -> Byte.compare(x.getmRssi(), y.getmRssi())).findFirst();

                    if (tag.isPresent()) {
                        String tagStr = TagToString.apply(tag.get());
                        if (!trimEPCFlag)
                            return tagStr;
                        else
                            return (tagStr.length() > 12) ? tagStr.substring(11) : "N/A";
                    } else {
                        return "";
                    }
                }
            }
            // to avoid possible endless loop.
            if (idx > 10)
                return null;
        }
    }
}
