package io.agritrack.philosofish.fish.state;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import java.util.List;
import java.util.Set;

import io.agritrack.philosofish.data.model.common.LaundrySample;
import io.agritrack.philosofish.data.model.common.TonneSample;

public class PackageQualityRecord {

    public Long txkey;

    public String lot;

    public Integer freshGrade;

    public Integer skingGrade;

    public Integer eye_grade;

    public Integer gill_grade;

    public Integer crookedMouth;

    public Integer lowerJaw;

    public Integer jawOver;

    public Integer operculum;

    public Integer lordosis;

    public Integer shortening;

    public Integer skeletical;

    public Integer tail;

    public Integer tailDeform;

    public Integer finDeform;

    public Integer woundsDeform;

    public Integer hemSlight;

    public Integer hemSpots;

    public Integer hemDiffuse;

    public Integer hemWounds;

    public Integer eyeBlurred;

    public Integer eyeCured;

    public Integer eyeBlind;

    public Integer eyeBleed;

    public Integer gillMucus;

    public Integer gillBloody;

    public Integer gillBrown;

    public Integer gillDiscolor;

    public Integer headDeform;

    public List<LaundrySample> laundrySamples;

    public List<TonneSample> tonneSamples;
}
