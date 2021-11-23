package io.agritrack.rfid;

import android.text.TextUtils;

import com.android.hdhe.uhf.reader.UhfReader;
import com.android.hdhe.uhf.readerInterface.TagModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

import cn.pda.serialport.Tools;

public class MultipleFilterSingleShotScanner implements Callable {

    private final Function<TagModel, String> TagToString = t -> Tools.Bytes2HexString(t.getmEpcBytes(), t.getmEpcBytes().length);
    private UhfReader uhfReader;
    private String[] RFID_FILTERS = null;

    public void setUhfReader(UhfReader uhfReader) {
        this.uhfReader = uhfReader;
        this.uhfReader.setOutputPower(33);
    }

    public void setFilters(String... rfidFilters) {
        this.RFID_FILTERS = rfidFilters;
    }

    @Override
    public Object call() throws Exception {
        int idx = 0;
        while (true) {
            idx++;
            if (uhfReader != null) {
                final List<TagModel> tagList = uhfReader.inventoryRealTime();
                if (tagList != null && !tagList.isEmpty()) {
                    List<String> result = new ArrayList<>();
                    List<Optional<TagModel>> optionalTags = filterTags(tagList);
                    for (Optional<TagModel> optionalTag : optionalTags) {
                        if (optionalTag.isPresent())
                            result.add(TagToString.apply(optionalTag.get()));
                    }
                    return TextUtils.join(",", result);
                }
            }
            // to avoid possible endless loop.
            if (idx > 10)
                return null;
        }
    }

    private List<Optional<TagModel>> filterTags(List<TagModel> tagList) {
        Set<Optional<TagModel>> filteredTags = new HashSet<>();
        for (String filter : this.RFID_FILTERS) {
            Optional<TagModel> aTag = tagList.stream().sorted((y, x) -> Byte.compare(x.getmRssi(), y.getmRssi())).filter(i -> TagToString.apply(i).indexOf(filter) == 11).findFirst();
            filteredTags.add(aTag);
        }
        return new ArrayList<>(filteredTags);
    }
}
