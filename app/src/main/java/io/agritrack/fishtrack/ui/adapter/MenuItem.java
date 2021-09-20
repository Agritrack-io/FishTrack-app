package io.agritrack.fishtrack.ui.adapter;


public class MenuItem {
    private String name;
    private String description;
    private int imgId;
    private Class clazz;

    public MenuItem(String name, Class activityClass, int imgID) {
        this.name = name;
        this.clazz = activityClass;
        this.imgId = imgID;
    }

    public MenuItem(String name, String description, Class activityClass) {
        this.name = name;
        this.description = description;
        this.clazz = activityClass;
    }

    public MenuItem(String name, String description, Class activityClass, int imgID) {
        this.name = name;
        this.description = description;
        this.clazz = activityClass;
        this.imgId = imgID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
