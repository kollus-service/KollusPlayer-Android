package com.kollus.se.kollusplayer.kollusapi.jwt;

public class SubtitlePolicy {
    SubtitleFilter filter;
    boolean show_by_fiter;
    boolean is_showable;
    public SubtitlePolicy(){}

    public SubtitleFilter getFilter() {
        return filter;
    }

    public void setFilter(SubtitleFilter filter) {
        this.filter = filter;
    }

    public boolean isShow_by_fiter() {
        return show_by_fiter;
    }

    public void setShow_by_fiter(boolean show_by_fiter) {
        this.show_by_fiter = show_by_fiter;
    }

    public boolean isIs_showable() {
        return is_showable;
    }

    public void setIs_showable(boolean is_showable) {
        this.is_showable = is_showable;
    }
}
