package com.webprobity.ms_spares_classified.modelsList;

public class catSubCatlistModel {

    private String id;
    private String cardName;
    private String path;
    private String price;
    private String date;
    private String location;
    private String adViews;
    private String imageResourceId;
    private int isfav;
    private int isturned;
    private boolean isSearchItem;
    private String favBtnText;
    private String favColorCode;
    private String addTypeFeature;
    private String type;
    public boolean isFeatureType=false;

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAdViews() {
        return adViews;
    }

    public void setAdViews(String adViews) {
        this.adViews = adViews;
    }

    public int getIsturned() {
        return isturned;
    }

    public void setIsturned(int isturned) {
        this.isturned = isturned;
    }

    public int getIsfav() {
        return isfav;
    }

    public void setIsfav(int isfav) {
        this.isfav = isfav;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(String imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    public boolean isSearchItem() {
        return isSearchItem;
    }

    public void setSearchItem(boolean searchItem) {
        isSearchItem = searchItem;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFavBtnText() {
        return favBtnText;
    }

    public void setFavBtnText(String favBtnText) {
        this.favBtnText = favBtnText;
    }

    public String getFavColorCode() {
        return favColorCode;
    }

    public void setFavColorCode(String favColorCode) {
        this.favColorCode = favColorCode;
    }

    public String getAddTypeFeature() {
        return addTypeFeature;
    }

    public void setAddTypeFeature(String addTypeFeature) {
        this.addTypeFeature = addTypeFeature;
    }
    public void setFeatureType(boolean featureType){
        this.isFeatureType=featureType;
    }
    public boolean getFeaturetype(){return  this.isFeatureType;}
}