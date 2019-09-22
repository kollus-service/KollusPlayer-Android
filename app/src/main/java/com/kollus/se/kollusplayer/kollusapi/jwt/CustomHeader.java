package com.kollus.se.kollusplayer.kollusapi.jwt;

public class CustomHeader {
    String key;
    String value;
    public CustomHeader(){
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
