package io.agritrack.fish.ui.bo;

import java.util.UUID;

/**
 * This class is intended for use in List Adapters.
 * It provides the 'Id' of an entity primary key, a label to be shown on a list
 *  and (if required) a check box to allow for multiple items selection.
 */
public class GenericListModel {

    public enum origin {Normal, Split, Offline};

    private UUID id;
    private String label, requestId;
    private String rfid, code;
    private Double netEyeGirth, perimeter;
    private String[] labels;
    private Boolean checked;
    private origin type = origin.Normal;

    public GenericListModel(UUID id, String rfid, String code, Double netEyeGirth, Double perimeter) {
        this.id = id;
        this.rfid = rfid;
        this.code = code;
        this.netEyeGirth = netEyeGirth;
        this.perimeter = perimeter;

//        this.label = netEyeGirth != null && netEyeGirth != 0.0 && perimeter != null ? String.format("%s %s/%.2f/%.2f", rfid, code, netEyeGirth, perimeter) :
//                ((netEyeGirth != null && netEyeGirth == 0.0) && perimeter != null ? String.format("%s %s/%.2f", rfid, code, perimeter) : rfid + " " + code);
    }

    public GenericListModel(UUID id, String label) {
        this.id = id;
        this.label = label;
        this.checked = Boolean.FALSE;
    }

    public GenericListModel(UUID id, String label, Boolean isChecked) {
        this.id = id;
        this.label = label;
        this.checked = isChecked;
    }

    public GenericListModel(UUID id, String[] labels, Boolean isChecked) {
        this.id = id;
        this.labels = labels;
        this.checked = isChecked;
    }

    public GenericListModel(String requestId, String label, origin type) {
        this(requestId,label);
        this.type = type;
    }

    public GenericListModel(String requestId, String format) {
        this.requestId = requestId;
        this.label = format;
    }

    public GenericListModel(String requestId, Boolean isChecked) {
        this.requestId = requestId;
        this.checked = isChecked;
    }

    public GenericListModel(String requestId, String label, Boolean isChecked) {
        this.requestId = requestId;
        this.label = label;
        this.checked = isChecked;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public String getRfid() {
        return rfid;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setNetEyeGirth(Double netEyeGirth) {
        this.netEyeGirth = netEyeGirth;
    }

    public Double getNetEyeGirth() {
        return netEyeGirth;
    }

    public void setPerimeter(Double perimeter) {
        this.perimeter = perimeter;
    }

    public Double getPerimeter() {
        return perimeter;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public origin getType() {
        return type;
    }

    public Boolean isChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    @Override
    public String toString() {
        return label;
    }

    public void setRequestd(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }
}
