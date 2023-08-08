package io.agritrack.caen.api;

import com.android.hdhe.uhf.readerInterface.TagModel;
import com.uhf.api.cls.Reader;

import java.util.function.Function;

import cn.pda.serialport.Tools;

public abstract class AbstractX9Programmer {
    private static final boolean LoggerIsOpen = Boolean.FALSE;
    protected final Function<TagModel, String> TagToString = t -> Tools.Bytes2HexString(t.getmEpcBytes(), t.getmEpcBytes().length);
    protected final Function<Reader.TAGINFO, String> TagInfoToString = t -> Tools.Bytes2HexString(t.EpcId, t.Epclen);

    // ##########################
    // ###  abstract methods  ###
    // ##########################
    abstract public void setFilterEPC(String epc);

    abstract public boolean clearEPCFilter();

    abstract public void HighPowerLevel();

    abstract public void LowPowerLevel();

    abstract public int[] getPowerLevel();

    abstract public Reader.READER_ERR writeTagEPC(String epc);

    abstract public Reader.READER_ERR writeTagEPCByFilter(String epc, String fdata);

    abstract public Reader.READER_ERR writeTagEPCByTIDFilter(String epc, String fdata);

    abstract public String getTagTIDDataByFilter(String epc);

    abstract public String getTagEpcDataByFilter(String tid);

    abstract public void stopProgramming();
}
