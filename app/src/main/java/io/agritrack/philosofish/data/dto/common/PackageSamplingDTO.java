package io.agritrack.philosofish.data.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.agritrack.philosofish.data.model.common.SortingSample;
import io.agritrack.philosofish.data.model.common.TonneSample;

public class PackageSamplingDTO {

    @SerializedName("sorting_samples")
    public List<SortingSample> sortingSamples;

    @SerializedName("tonne_samples")
    public List<TonneSample> tonneSamples;
}
