package com.kollus.se.kollusplayer.kollusapi.multidrm.tokenrule;

public enum DrmTypes {
    NCG("NCG"),
    Widevine("Widevine"),
    PlayReady("PlayReady"),
    FairPlay("FairPlay");

    private String value;
    private DrmTypes(String type){
        this.value = type;
    }
    public String getType(){
        return this.value;
    }
}
