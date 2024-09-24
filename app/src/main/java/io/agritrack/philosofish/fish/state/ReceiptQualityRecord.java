package io.agritrack.philosofish.fish.state;

public class ReceiptQualityRecord {

    public long txkey;
    public String lot;
    public String cage;
    public String fishingDate;
    public String fishSpecies;
    public String farm, plant;
    public String arrivalTime;
    public String startTime;
    public Boolean binSeal = true;
    public Integer eyeRating;
    public Integer gillRating;
    public Integer fleshRating;
    public Integer skinRating;
    public Integer disEyes;
    public Integer disTail;
    public Integer disSkeletal;
    public Integer disBlood;
    public Integer disMouth;
    public Integer disOper;
    public String comments;

    public ReceiptQualityRecord() {
    }
}
