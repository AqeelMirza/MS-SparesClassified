package com.webprobity.ms_spares_classified.modelsList;

public class subcatDiloglist {

    public String id;
    private boolean hasSub;
    public String name;
    private boolean hasCustom;
    private boolean isShow;

    public boolean isHasCustom() {
        return hasCustom;
    }

    public void setHasCustom(boolean hasCustom) {
        this.hasCustom = hasCustom;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isHasSub() {
        return hasSub;
    }

    public void setHasSub(boolean hasSub) {
        this.hasSub = hasSub;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }
}
