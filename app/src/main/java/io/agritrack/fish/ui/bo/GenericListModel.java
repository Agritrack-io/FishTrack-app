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
    private String[] labels;
    private Boolean checked;
    private origin type = origin.Normal;

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
