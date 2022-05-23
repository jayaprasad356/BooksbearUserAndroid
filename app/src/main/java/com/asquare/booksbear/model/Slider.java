package com.asquare.booksbear.model;


public class Slider {
    final String image;
    String type;
    String type_id;
    String name;
    String slide_url;

    public Slider(String type, String type_id, String name, String image , String slide_url) {
        this.type = type;
        this.type_id = type_id;
        this.name = name;
        this.image = image;
        this.slide_url = slide_url;
    }


    public Slider(String image) {
        this.image = image;
    }

    public String getType_id() {
        return type_id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getType() {
        return type;
    }

    public String getSlide_url() {
        return slide_url;
    }

    public void setSlide_url(String slide_url) {
        this.slide_url = slide_url;
    }
}
