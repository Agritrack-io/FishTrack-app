package io.agritrack.fishtrack.ui.activity.adapter;


public class HomeMenuItem {
    private String name;
    private int imgId;
    private String activity;

    public HomeMenuItem(String name, String activity, int imgID) {
        this.name = name;
        this.activity = activity;
        this.imgId = imgID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }
}
