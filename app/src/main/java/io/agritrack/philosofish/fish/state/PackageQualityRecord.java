package io.agritrack.philosofish.fish.state;

import java.util.List;

import io.agritrack.philosofish.data.model.common.SortingSample;
import io.agritrack.philosofish.data.model.common.TonneSample;

public class PackageQualityRecord {

    public Long createdAt;

    public String lot;

    public String species;

    public String bestBefore;

    public Integer freshGrade;

    public Integer skinGrade;

    public Integer eyeGrade;

    public Integer gillGrade;

    public Integer overallGrade;

    public Integer crookedMouth;

    public Integer lowerJaw;

    public Integer jawOver;

    public Integer operculum;

    public Integer lordosis;

    public Integer shortening;

    public Integer skeletical;

    public Integer tailDeformity;

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

    public List<SortingSample> sortingSamples;

    public List<TonneSample> tonneSamples;

    public Boolean startPacking;

    public Boolean changePacking;

    public Boolean middlePacking;

    public Boolean endPacking;

    public String labelComments;
}
