package io.agritrack.fishtrack.ui.adapter;


public class MenuItem {
    private String name;
    private int imgId;
    private Class clazz;

    public MenuItem(String name, Class activityClass, int imgID) {
        this.name = name;
        this.clazz = activityClass;
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

    public Class getActivity() {
        return clazz;
    }

    public void setActivity(Class activity) {
        this.clazz = activity;
    }
}
