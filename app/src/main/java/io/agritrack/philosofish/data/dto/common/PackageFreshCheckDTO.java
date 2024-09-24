package io.agritrack.philosofish.data.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PackageFreshCheckDTO {

    @JsonProperty("fresh_grade")
    public Integer freshGrade;

    @JsonProperty("skin_grade")
    public Integer skinGrade;

    @JsonProperty("eye_grade")
    public Integer eyeGrade;

    @JsonProperty("gill_grade")
    public Integer gillGrade;

    @JsonProperty("crooked_mouth")
    public Integer crookedMouth;

    @JsonProperty("lower_law")
    public Integer lowerJaw;

    @JsonProperty("jaw_over")
    public Integer jawOver;

    @JsonProperty("overall_grade")
    public Integer overallGrade;

    @JsonProperty("operculum")
    public Integer operculum;

    @JsonProperty("lordosis")
    public Integer lordosis;

    @JsonProperty("shortening")
    public Integer shortening;

    @JsonProperty("skeletical")
    public Integer skeletical;

    @JsonProperty("tail_deformity")
    public Integer tailDeformity;

    @JsonProperty("tail_deform")
    public Integer tailDeform;

    @JsonProperty("fin_deform")
    public Integer finDeform;

    @JsonProperty("wound_deform")
    public Integer woundsDeform;

    @JsonProperty("hem_slight")
    public Integer hemSlight;

    @JsonProperty("hem_spots")
    public Integer hemSpots;

    @JsonProperty("hem_diffuse")
    public Integer hemDiffuse;

    @JsonProperty("hem_wounds")
    public Integer hemWounds;

    @JsonProperty("eye_blurred")
    public Integer eyeBlurred;

    @JsonProperty("eye_cured")
    public Integer eyeCured;

    @JsonProperty("eye_blind")
    public Integer eyeBlind;

    @JsonProperty("eye_bleed")
    public Integer eyeBleed;

    @JsonProperty("gill_mucus")
    public Integer gillMucus;

    @JsonProperty("gill_bloody")
    public Integer gillBloody;

    @JsonProperty("gill_brown")
    public Integer gillBrown;

    @JsonProperty("gill_discolor")
    public Integer gillDiscolor;

    @JsonProperty("head_deform")
    public Integer headDeform;
}
