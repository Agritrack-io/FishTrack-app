package io.agritrack.philosofish.data.dto.tx;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import io.agritrack.philosofish.data.model.tx.ReceiptQualityTransaction;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class ReceiptQualityTxDTO {
    private static final SimpleDateFormat simpleDate =  new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat simpleDateTime =  new SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ss");
    private static final SimpleDateFormat simpleTime =  new SimpleDateFormat("HH:mm");

    public UUID id;

    //@JsonFormat(pattern = "dd/MM/yyyy'T'HH:mm:ss")
    public String occurred_at;
    public String lot;
    public String cage;
    public String fishing_date;
    public String species;
    public String user;
    public String plant;
    public String arrival_time;
    public String start_time;
    public Boolean bin_seal;
    public Integer eye_rating;
    public Integer gill_rating;
    public Integer flesh_rating;
    public Integer skin_rating;
    public Integer dis_eyes;
    public Integer dis_tail;
    public Integer dis_skeletal;
    public Integer dis_blood;
    public Integer dis_mouth;
    public Integer dis_oper;
    public String comments;

    public static ReceiptQualityTxDTO convert(ReceiptQualityTransaction quality) {
        ReceiptQualityTxDTO qualityTxDto = new ReceiptQualityTxDTO();
        qualityTxDto.id = quality.id;
        qualityTxDto.lot = quality.lot;
        qualityTxDto.cage = quality.cage;
        qualityTxDto.arrival_time = simpleTime.format(quality.arrivalTime);
        qualityTxDto.start_time = simpleTime.format(quality.startTime);
        qualityTxDto.bin_seal = quality.sealed;
        qualityTxDto.eye_rating = quality.eyeRating;
        qualityTxDto.gill_rating = quality.gillRating;
        qualityTxDto.skin_rating = quality.skinRating;
        qualityTxDto.flesh_rating = quality.fleshRating;
        qualityTxDto.dis_blood = quality.disBlood;
        qualityTxDto.dis_eyes = quality.disEyes;
        qualityTxDto.dis_mouth = quality.disMouth;
        qualityTxDto.dis_oper = quality.disOper;
        qualityTxDto.dis_tail = quality.disTail;
        qualityTxDto.plant = LocalPreferences.getCurrentSiteName();
        qualityTxDto.dis_skeletal = quality.disSkeletal;
        qualityTxDto.fishing_date = simpleDate.format(quality.fishingDate);
        qualityTxDto.species = quality.species;
        qualityTxDto.comments = quality.comments;
        qualityTxDto.occurred_at = simpleDateTime.format(new Date(quality.createdAt));
        qualityTxDto.user = quality.user;
        return qualityTxDto;
    }
}
