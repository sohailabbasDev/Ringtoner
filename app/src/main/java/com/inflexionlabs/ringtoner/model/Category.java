package com.inflexionlabs.ringtoner.model;

public class Category {
    private String text;
    private String url;

    public Category() {
    }


    public String getText() {
        return text;
    }

    public String getUrl() {
        return url;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}