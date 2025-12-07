package io.agritrack.philosofish.ui.adapter;


import java.util.Objects;

public class MenuItem {
    private Integer loc;
    private String name;
    private String description;
    private int imgId;
    private Class clazz;

    public boolean isDone = false;

    public MenuItem(String name, Class activityClass, int imgID) {
        this.name = name;
        this.clazz = activityClass;
        this.imgId = imgID;
    }

    public MenuItem(String name, Class activityClass) {
        this.name = name;
        this.clazz = activityClass;
    }

    public MenuItem(Integer loc, String name, Class activityClass, int imgID) {
        this.loc = loc;
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

    public int getLoc() {
        return loc;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return Objects.equals(clazz, menuItem.clazz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, clazz);
    }


}
