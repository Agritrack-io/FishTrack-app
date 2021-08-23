package io.agritrack.fishtrack.ui.bo;

/**
 * This class is intended for use in List Adapters.
 * It provides the 'Id' of an entity primary key, a label to be shown on a list
 *  and (if required) a check box to allow for multiple items selection.
 */
public class GenericListModel {

    private Long id;
    private String label;
    private Boolean checked;

    public GenericListModel(Long id, String label) {
        this.id = id;
        this.label = label;
        this.checked = Boolean.FALSE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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
}
